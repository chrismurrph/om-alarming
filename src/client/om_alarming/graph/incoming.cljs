(ns om-alarming.graph.incoming
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! timeout alts!]]
            [om-alarming.util.utils :refer [log]]
            [om-alarming.graph.mock-values :as db]
            [om-alarming.util.utils :as u]
            [om-alarming.sente-client :as client]
            [cljs-time.format :as format-time]
            [cljs-time.coerce :as coerce]
            [om.next :as om]
            )
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

;;
;; First will be exactly at start
;;
(defn create-n-times [n start end]
  (assert (not (nil? start)))
  (assert (not (nil? end)))
  (assert (> end start))
  (let [diff (- end start)
        increment (quot diff n)
        res (map (fn [idx] (+ start (* increment idx))) (range n))
        ;Weird assertion - if starts at 0 will be triggered
        ;_ (assert (empty? (filter #(= % 0) res)) (str "Expected a wide enough range could go across without needing fractions: " start " " end " " increment))
        ]
    res))

(def gas-gen-quantity 50)

;;
;; Whenever its out channel is not blocked it will be generating a new gas value
;; There is a generator for each line
;; Will just stop looping when it finishes, so no need for a stop function - hmm - unless the
;; user no longer wants to see more values - hmm - not worth effort for random generating code!
;;
#_(defn generator [start end info out-chan]
    (assert out-chan)
    (assert (nil? (:name info)) "No longer using name")
    (assert (:ident info) "Must have ident")
    (assert (> end start) (str "end: " end ", must be greater than start: " start))
    (let [all-times (create-n-times gas-gen-quantity start end)]
      (go-loop [completed []]
               (when (not= (count completed) (count all-times))
                 (let [available (remove (into #{} completed) all-times)
                       picked-time (nth available (rand-int (count available)))]
                   (>! out-chan {:info info :val (db/random-gas-value (:ident info)) :time picked-time})
                   (recur (conj completed picked-time)))))))

(defn- chk-params [start end out-chan]
  (assert out-chan)
  (assert (> end start) (str "end: " end ", must be greater than start: " start))
  )

;;
;; Do a query on the server - this is more realistic
;;
(defn batch-generator [start end info out-chan]
  (chk-params start end out-chan)
  (let [all-times (create-n-times gas-gen-quantity start end)]
    (go-loop [completed [] batched []]
             (if (not= (count completed) (count all-times))
               (let [available (remove (into #{} completed) all-times)
                     picked-time (nth available (rand-int (count available)))
                     new-gen {:val (db/random-gas-value (:ident info)) :time picked-time}
                     _ (println "info:" info)
                     _ (println "batched:" batched)]
                 (recur (conj completed picked-time) (conj batched new-gen)))
               (>! out-chan {:info info :vals batched})))))


(comment (client/chsk-send!
           [:example/points
            {:start-time-str "01_03_2016__09_08_02.948"
             :end-time-str   "07_03_2016__09_10_36.794"
             :metric-name    "Oxygen"
             :display-name   "Shed Tube 10"}] 5000
           (fn [cb-reply] (client/->output! "Example Callback reply: %s" cb-reply))))

(comment info {:ref [:line/by-id 103], :lowest 0.5, :highest 1.35, :ident [:gas-at-location/by-id 503]})

(def date-time-formatter (format-time/formatter "dd_MM_yyyy__HH_mm_ss.SSS"))
;;
;; Get back the time as a String so need to convert it:
;; 1. parse to goog date
;; 2. coerce/to-long to then get a number that we need internally
;; 3. check this time is between orig numbers we asked for
;;
(defn server-time->here-time [start end server-time-in]
  (let [_ (println (str "To examine: " server-time-in))
        goog-date (format-time/parse date-time-formatter server-time-in)
        milliseconds (coerce/to-long goog-date)
        _ (assert (>= milliseconds start) (str "Bad that falls before start: " milliseconds ", where start is " start))
        _ (assert (<= milliseconds end) (str "Bad that falls after end: " milliseconds ", where end is " end))]
    milliseconds))

(defn remote-query [start end info out-chan]
  (chk-params start end out-chan)
  (let [start-date-time (coerce/to-date-time start)
        end-date-time (coerce/to-date-time end)
        flight-start (format-time/unparse date-time-formatter start-date-time)
        flight-end (format-time/unparse date-time-formatter end-date-time)
        convert-time (partial server-time->here-time start end)
        ]
    ;(println "start, end" flight-start flight-end)
    ;(println "info" info)
    (client/chsk-send!
      [:example/points
       {:start-time-str flight-start
        :end-time-str   flight-end
        :metric-name    (:metric-name info)
        :display-name   (:display-name info)}] 5000
      (fn [cb-reply]
        (let [;_ (client/->output! "Callback reply: %s" cb-reply)
              incoming (mapv (fn [in] {:val (:val in) :time (convert-time (:time in))}) (:some-reply cb-reply))]
          (async/put! out-chan {:info info :vals incoming}))))))

;;
;; Just needs the channels it is going to get values from
;; Will just stop recuring when the chans have run out of values
;;
(defn controller-component [out-chan chans-in]
  (println "[controller-component] starting")
  (let [poison-ch (chan)
        chans (conj chans-in poison-ch)
        ;_ (log "CHANs:" (into [] chans))
        ]
    (go-loop []
             (let [[next-val ch] (alts! chans)]
               (if (= ch poison-ch)
                 (println "[controller-component] stopping")
                 (do
                   (<! (timeout 50))
                   (>! out-chan next-val)
                   (recur)))))
    (fn stop! []
      (close! poison-ch))))

(defn query-remote-server
  "Just needs the names that are to be queried and start/end times"
  [line-infos start end]
  (let [new-gen (partial remote-query start end)
        out-chan (chan)
        gas-channels (into {} (map (fn [info] (vector info (chan))) line-infos))
        ;_ (log gas-channels)
        stop-fn (controller-component out-chan (vals gas-channels))
        _ (mapv (fn [[info chan]] (new-gen info chan)) gas-channels)
        ]
    [stop-fn out-chan]
    )
  )

;;
;; Directly puts dots on the screen. Really it is staging-area's job to do this intelligently. So this will go.
;;
;(def tick-timer
;  (let [already-gone (fn [already-sent name x] (some #{{:line-name name :x x}} already-sent))]
;    (go-loop [already-sent []]
;             (<! (timeout 1000))
;             ;(log "In timer")
;             (let [line-num (rand-int 2)
;                   line (nth @db/lines line-num)
;                   name (:name line)
;                   line-size (count (:positions line))
;                   chosen-idx (rand-int line-size)
;                   position (nth (:positions line) chosen-idx)
;                   chosen-x-pos (:x position)
;                   has-gone (already-gone already-sent name chosen-x-pos)
;                   ]
;               (if has-gone
;                 (recur already-sent)
;                 (do
;                   ;(log name " at " position " about to go... ")
;                   (g/add-point-by-sa {:name name :point [chosen-x-pos (:y position) (:val position)]})
;                   (recur (conj already-sent {:line-name name :x chosen-x-pos}))))
;               ))))
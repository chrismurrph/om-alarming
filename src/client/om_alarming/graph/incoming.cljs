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

(defn- chk-params [start end out-chans]
  (assert out-chans)
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
           [:graph/points
            {:start-time-str "01_03_2016__09_08_02.948"
             :end-time-str   "07_03_2016__09_10_36.794"
             :metric-name    "Oxygen"
             :display-name   "Shed Tube 10"}]
           5000
           (fn [cb-reply] (client/->output! "Example Callback reply: %s" cb-reply))))

(comment info {:ref [:line/by-id 103], :best 0.5, :worst 1.35, :ident [:gas-at-location/by-id 503]})

;;
;; We have start and end times in millis, which server must be rounding, because it can send back times that are
;; slightly before when we requested. These functions allow us to accept back seemingly slightly out/odd results
;;
(defn back-sec [millis]
  (* 1000 (quot millis 1000)))
(defn forward-sec [millis]
  (+ 1000 (* 1000 (quot millis 1000))))

(def date-time-formatter (format-time/formatter "dd_MM_yyyy__HH_mm_ss.SSS"))
;;
;; Get back the time as a String so need to convert it:
;; 1. parse to goog date
;; 2. coerce/to-long to then get a number that we need internally
;; 3. check this time is between orig numbers we asked for
;;
(defn server-time->long-time [start end server-time-in]
  (let [;_ (println (str "To examine: " server-time-in))
        goog-date (format-time/parse date-time-formatter server-time-in)
        milliseconds (coerce/to-long goog-date)
        _ (assert (>= milliseconds (back-sec start)) (str "Bad that falls before start: " milliseconds ", where start is " start))
        _ (assert (<= milliseconds (forward-sec end)) (str "Bad that falls after end: " milliseconds ", where end is " end))]
    milliseconds))

(defn receive-for-location
  "infos are only for the display name asking for. The reply will return vectors in the same order"
  [flight-start flight-end in-chan convert-time display-name infos]
  (client/chsk-send!
    [:graph/points
     {:start-time-str flight-start
      :end-time-str   flight-end
      :metric-names   (map :metric-name infos)
      :display-name   display-name}] 5000
    (fn [cb-reply]
      (let [;_ (client/->output! "Callback reply: %s" cb-reply)
            all-gas-data (:some-reply cb-reply)
            all-data (map (fn [info datum] (into {} [[:info info] [:points datum]])) infos all-gas-data)
            ]
        (doseq [{:keys [points info]} all-data]
          (let [_ (println "INFO" info "\nPOINTS:" (count points) "\n")
                incoming (mapv (fn [in] {:val (-> in :val) :time (convert-time (-> in :time))}) points)]
            (async/put! in-chan {:info info :vals incoming})))))))

(defn remote-query [start end in-chan infos]
  (chk-params start end in-chan)
  (let [start-date-time (coerce/to-date-time start)
        end-date-time (coerce/to-date-time end)
        flight-start (format-time/unparse date-time-formatter start-date-time)
        flight-end (format-time/unparse date-time-formatter end-date-time)
        convert-time (partial server-time->long-time start end)
        receiver (partial receive-for-location flight-start flight-end in-chan convert-time)
        grouped-by-location (group-by :display-name infos)
        display-names (distinct (keys grouped-by-location))
        ]
    (println "start, end" flight-start flight-end)
    (println "for display-names:" display-names)
    (doseq [display-name display-names]
      (let [infos (get grouped-by-location display-name)]
        (receiver display-name infos)))))

;;
;; Just needs the channels it is going to get values from
;; Will just stop recuring when the chans have run out of values
;;
(defn controller-component [out-chan in-chan]
  (println "[controller-component] starting")
  (let [poison-ch (chan)
        chans [in-chan poison-ch]
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
  (let [in-chan (chan)
        ;new-gen (partial remote-query start end in-chan)
        out-chan (chan)
        ;gas-channels (into {} (map (fn [info] (vector info (chan))) line-infos))
        ;_ (println gas-channels)
        stop-fn (controller-component out-chan in-chan)
        ]
    (remote-query start end in-chan line-infos)
    #_(doseq [[inf ch] gas-channels]
      )
    [stop-fn out-chan]
    )
  )
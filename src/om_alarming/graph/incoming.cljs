(ns om-alarming.graph.incoming
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout alts!]]
            [om-alarming.utils :refer [log]]
            [om-alarming.graph.known-data-model :as db]
            ;[graphing.graphing :as g]
            [om-alarming.utils :as u]
            )
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

;;
;; First will be exactly at start
;;
(defn create-n-times [n start end]
  (let [diff (- end start)
        increment (quot diff n)
        res (map (fn [idx] (+ start (* increment idx))) (range n))
        _ (assert (empty? (filter #(= % 0) res)) "Expected a wide enough range could go across without needing fractions")]
    res))

;(log (create-150-times 500 2000))

;;
;; Whenever its out channel is not blocked it will be generating a new gas value
;; There is a generator for each line
;;
(defn generator [start end name out-chan]
  (assert (> end start) "end must be greater than start")
  (let [all-times (create-n-times 50 start end)]
    (go-loop [completed []]
             (when (not= (count completed) (count all-times))
               (let [available (remove (into #{} completed) all-times)
                     picked-time (nth available (rand-int (count available)))]
                 (>! out-chan {:name name :val (db/random-gas-value name) :time picked-time})
                 (recur (conj completed picked-time)))))))

;;
;; Just needs the channels it is going to get values from
;;
(defn controller [out-chan chans]
  (log (seq chans))
  (go-loop []
    (<! (timeout 300))
    (let [[next-val c] (alts! chans)
          _ (>! out-chan next-val)]
      (recur))
  ))

(defn query-remote-server
  "Just needs the names that are to be queried and start/end times"
  [names start end]
  (let [new-gen (partial generator start end)
        out-chan (chan)
        gas-channels (into {} (map (fn [name] (vector name (chan))) names))
        _ (log gas-channels)
        _ (controller out-chan (vals gas-channels))
        _ (mapv (fn [[name chan]] (new-gen name chan)) gas-channels)
        ]
    out-chan
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
(ns om-alarming.convert
  (:require [om-alarming.util :as u]
            [taoensso.timbre :as timbre :refer (tracef info infof warnf errorf)]))

(defn line->points [graph-line]
  (map #(.getGraphPoint graph-line %) (range (.size graph-line))))

;;
;; Discovered from this debugging that 02 comes thru as:
;; graphPoint: 0./ avgVal: null, avgValTimeStr: 05_05_2016__14_40_18.000, sampleTimeStr: 05_05_2016__14_40_18.000, TYPE: Minutely
;; i.e. null, which means that Client will receive an empty vector.
;;
(defn multigas->outs
  "There should only be one line, but retrieve all its points"
  [multigasReqDO]
  (let [gas-names (into [] (.getGasNamesList multigasReqDO))
        ;_ (infof "gas names: %s\n" gas-names)
        graph-lines (map #(.getGraphLine multigasReqDO %) gas-names)
        #_(doseq [graph-line graph-lines]
            (infof "graph-line size: %s\n" (.size graph-line))
            (infof "graph-line: %s\n" graph-line)
            )
        ]
    (map line->points graph-lines)))

(defn new-type-from-old [old-type-val]
  ;(debugf "TRANSFORM a %s" old-type-val)
  (let [name (.getName old-type-val)
        new-val (case name
                  "Ten Minutely" :ten-mintely
                  "Hourly" :hourly
                  "Minutely" :minutely)]
    new-val))

(defn simplify-type [in-map]
  (let [existing-type (:type in-map)
        new-type (new-type-from-old existing-type)]
    (assoc in-map :type new-type)))

(defn make-as-expected
  "At the moment the client only wants to see val and time"
  [in-map]
  (let [{:keys [maxVal minVal maxValTimeStr minValTimeStr]} in-map
        _ (assert (and (nil? maxVal) (nil? minVal) (nil? maxValTimeStr) (nil? minValTimeStr)) "In T/B only expect to see avgVal, which is an actual reading")])
  (assoc (dissoc in-map :maxVal :minVal :avgVal :sampleTimeStr :maxValTimeStr :minValTimeStr) :val (:avgVal in-map) :time (:sampleTimeStr in-map)))

(defn points->vec
  "Takes a list of points returned from Java Server and changes each into Clojure style data"
  [list]
  (->> list
       (map bean)
       (map #(u/unselect-keys % [:class]))
       (map simplify-type)
       (map make-as-expected)
       (filter #(:val %))
       (vec)
       ))



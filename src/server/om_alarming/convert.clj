(ns om-alarming.convert)

(defn multigas->out
  "There should only be one line, but retrieve all its points"
  [multigasReqDO]
  (let [gas-names (into [] (.getGasNamesList multigasReqDO))
        ;_ (debugf "gas names: %s\n" gas-names)
        _ (assert (= 1 (count gas-names)) "Expect client to only ask for one gas name at a time")
        graph-line (first (map #(.getGraphLine multigasReqDO %) gas-names))
        ;_ (debugf "graph-line: %s\n" graph-line)
        ;_ (debugf "graph-line size: %s\n" (.size graph-line))
        ]
    (map #(.getGraphPoint graph-line %) (range (.size graph-line)))))

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



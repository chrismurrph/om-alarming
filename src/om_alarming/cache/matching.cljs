(ns om-alarming.cache.matching)

;;
;; :want-b4-have -> cutoff before what already have
;; :want-after-have -> cutoff after what already have
;; What comes before/after these two cutoffs may be small or large, and become pools
;;
(defn- range-satisfies
  [have-date-range want-date-range]
  "If have starts before (or at) and ends after (or at) want"
  (let [want-b4-have (- (:start have-date-range) (:start want-date-range))
        want-after-have (- (:end want-date-range) (:end have-date-range))
        satisfied-b4 (or (zero? want-b4-have) (neg? want-b4-have))
        satisfied-after (or (zero? want-after-have) (neg? want-after-have))]
    ;(println "RES: " want-b4-have want-after-have)
    {;:satisfies?      (and satisfied-b4 satisfied-after)
     :want-b4-have    (when (not satisfied-b4) want-b4-have)
     :want-after-have (when (not satisfied-after) want-after-have)}))

(defn- create-ranges-from-cutoffs [{:keys [start end]} {:keys [want-b4-have want-after-have]}]
  (let [before-range {:start start :end (+ start want-b4-have)}
        after-range {:start (- end want-after-have) :end end}
        both [before-range after-range]]
    both))

(defn refine-need [have-date-range want-date-range]
  (let [cutoffs (range-satisfies have-date-range want-date-range)
        ranges (create-ranges-from-cutoffs want-date-range cutoffs)]
    ranges))

(defn overlap [range1 range2]
  (let [start1 (:start range1)
        end1 (:end range1)
        start2 (:start range2)
        end2 (:end range2)]
    (and (> end1 start2) (< start1 end2))))

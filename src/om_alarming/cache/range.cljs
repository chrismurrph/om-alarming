(ns om-alarming.cache.range)

(defn range-dur [{:keys [start end]}]
  (- end start))

(defn range-nil? [{:keys [start end]}]
  (or (nil? start) (nil? end)))

(defn range-zero? [{:keys [start end] :as rng}]
  (zero? (range-dur rng)))

(defn range-blank? [{:keys [start end] :as rng}]
  (or (range-zero? rng) (range-nil? rng)))

(defn lesser-of [x y]
  (if (< x y) x y))

(defn greater-of [x y]
  (if (> x y) x y))

;;
;; If begin is earlier replace it. If end later replace it.
;;
(defn sum-ranges [ranges]
  ;(println "SUM: " ranges)
  (reduce (fn [acc ele]
            (let [{:keys [start end] as range} ele
                  current-start (:start acc)
                  current-end (:end acc)]
              {:start (lesser-of start current-start)
               :end (greater-of end current-end)}))
          ranges))

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

;;
;; Substracting have-date-range from want-date-range
;;
(defn refine-need [have-date-range want-date-range]
  (if (range-blank? have-date-range)
    [want-date-range]
    (let [cutoffs (range-satisfies have-date-range want-date-range)
          ranges (create-ranges-from-cutoffs want-date-range cutoffs)]
      ranges)))


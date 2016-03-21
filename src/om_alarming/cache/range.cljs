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
;; Does not work if there are gaps, but order does not matter
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

(defn same-range [range1 range2]
  (when
    (and (= (:start range1) (:start range2))
         (= (:end range1) (:end range2)))
    range2))

;;
;; What part of the existing range can be grabbed for the new range.
;; We return a range that can be stolen from existing-range.
;; Another function will do the stealing
;;
(defn covet [want-range existing-range]
  (let [start-want (:start want-range)
        end-want (:end want-range)
        start-existing (:start existing-range)
        end-existing (:end existing-range)
        want-ahead (and (< start-existing end-want) (> end-existing start-want))
        existing-ahead (and (> end-existing start-want) (< start-existing end-want))
        ;a-clipped-by-b (= start-want end-existing)
        ;b-clipped-by-a (= start-existing end-want)
        ]
    (let [res (when (or want-ahead
                        existing-ahead
                        ;(or a-clipped-by-b b-clipped-by-a)
                        )
                (cond
                  (and want-ahead existing-ahead) {:start (greater-of start-want start-existing) :end (lesser-of end-want end-existing)}
                  want-ahead {:start start-want :end end-existing}
                  existing-ahead {:start start-existing :end end-want}
                  ;a-clipped-by-b {:start start-want :end end-existing}
                  ;b-clipped-by-a {:start start-existing :end end-want}
                  ))]
      ;(assert (not (same-range res want-range)) (str "Perfect steal s/not be possible: <" res want-range ">"))
      res)
    ))
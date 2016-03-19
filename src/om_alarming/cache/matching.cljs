(ns om-alarming.cache.matching)

(defn same-range [range1 range2]
  (when
    (and (= (:start range1) (:start range2))
         (= (:end range1) (:end range2)))
    range2))

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

(defn lesser-of [x y]
  (if (< x y) x y))

(defn greater-of [x y]
  (if (> x y) x y))

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
      (assert (not (same-range res want-range)) "Perfect steal s/not be possible - handle when get (expect only when zooming in)")
      res)
    ))

;;
;; Returns what the existing range will be left with when what is coveted has been stolen
;;
(defn steal [coveted-range existing-range]
  (let [start-want (:start coveted-range)
        end-want (:end coveted-range)
        start-existing (:start existing-range)
        end-existing (:end existing-range)]))

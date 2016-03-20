(ns om-alarming.cache.matching
  (:require [om-alarming.cache.range :as rng]))

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
              (and want-ahead existing-ahead) {:start (rng/greater-of start-want start-existing) :end (rng/lesser-of end-want end-existing)}
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
        end-existing (:end existing-range)
        steal-from-beginning (= start-want start-existing)
        steal-from-end (= end-want end-existing)]
    (assert (and (or steal-from-beginning steal-from-end)
                 (not (and steal-from-beginning steal-from-end)))
            "Can only steal from against one of the edges")
    (cond steal-from-beginning {:start (inc end-want) :end end-existing}
          steal-from-end {:start start-existing :end (dec start-want)})))

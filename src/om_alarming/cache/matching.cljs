(ns om-alarming.cache.matching
  (:require [om-alarming.cache.range :as rng]))

(defn same-hold [hold1 hold2]
  (when
    (and (= (:id hold1) (:id hold2))
         (rng/same-range hold1 hold2)
         )
    hold2))

(defn existing-hold
  "Returns first equivalent hold from the pool"
  [hold pool-of-holds]
  (let [equiv-fn (partial same-hold hold)]
    (some equiv-fn (vals pool-of-holds))))

(defn re-point-existing-hold
  "Points existing at the to-uid - my data when it comes back is needed by another pool"
  [existing-hold to-uid]
  (-> existing-hold
      (merge {:kept-to-fill {:uid to-uid}})
      (dissoc :cb))
  )

#_(defn overlap-hold
  "Returns the first overlapping hold from the pool"
  [hold pool-of-holds]
  (let [equiv-fn (partial covet hold)]
    (some equiv-fn (vals pool-of-holds))))

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

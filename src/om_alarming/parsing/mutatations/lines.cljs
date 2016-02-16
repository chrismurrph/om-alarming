(ns om-alarming.parsing.mutations.lines
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]
            [om-alarming.parsing.mutations.points :as points]
            [default-db-format.core :as db-format]))

(defn add-to-points [points ref]
  (println "Adding: " ref " to: " points)
  (into [] (cond-> points
                   (not (some #{ref} points)) (conj ref))))

;;
;; To create a point we need to know the name of the line that it is to go in
;; and x and y.
;;
(defn create-point [st params]
  (let [{:keys [line-name-ident x y]} params
        {:keys [state point]} (points/new-point st x y)
        ;; Do not get executed so why bother
        ;_ (assert (db-format/ident? "by-id" line-name-ident))
        ;_ (println "Received x y: " x y line-name-ident)
        ;_ (assert (and state point))
        ;_ (println "Created point: " point)
        ] 
    (-> state
        (update-in (conj line-name-ident :graph/points) add-to-points line-name-ident)
        (assoc :graph/points point)
        )
    ))

;;
;; params need to be line Ident, x and y
;;
(defmethod mutate 'graph/add-point
  [{:keys [state]} _ params]
  {:action #(swap! state create-point params)})

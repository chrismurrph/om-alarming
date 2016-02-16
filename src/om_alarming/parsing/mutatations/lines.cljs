(ns om-alarming.parsing.mutations.lines
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]
            [om-alarming.parsing.mutations.points :as points]))

(defn add-to-points [points ref]
  (into [] (cond-> points
                   (not (some #{ref} points)) (conj ref))))

;;
;; To create a point we need to know the name of the line that it is to go in
;; and x and y.
;;
(defn create-point [st params]
  (let [{:keys [line-name-ident line-name x y]} params
        _ (assert (and line-name-ident line-name x y))
        {:keys [state point]} (points/create-point st x y)]
    (-> state
        (update-in (conj line-name-ident :graph/points) add-to-points line-name-ident)
        (assoc :graph/points point))))

;;
;; params need to be line Ident, x and y
;;
(defmethod mutate 'graph/add-point
  [{:keys [state]} _ params]
  {:action #(swap! state create-point params)})

(ns om-alarming.parsing.mutations.lines
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]
            [om-alarming.parsing.mutations.points :as points]))

;;
;; To create a point we need to know the name of the line that it is to go in
;; and x and y.
;;
(defn create-point [st params]
  (let [{:keys [line-name x y]} params
        _ (assert (and line-name x y))
        {:keys [state point]} (points/create-point st x y)]
    (-> state
        (update-in (conj lane :cards) add-to-cards card)
        (assoc :cards/editing card))))

;;
;; params need to be line Ident, x and y
;;
(defmethod mutate 'graph/add-point
  [{:keys [state]} _ params]
  {:action #(swap! state create-point params)})

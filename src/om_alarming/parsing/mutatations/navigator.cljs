(ns om-alarming.parsing.mutations.navigator
  (:require [om-alarming.reconciler :refer [mutate]]
            [cljs-time.core :as time]))

;;
;; Will have to reduce over every line ident with state being the accumulator
;;
(defn rm-all-points-from-line [st line-ident]
  (-> st
      (assoc-in (conj line-ident :graph/points) [])))

(defn rm-all-points [st]
  (let [all-lines-idents (:graph/lines st)]
    (-> (reduce rm-all-points-from-line
                st
                all-lines-idents)
        (assoc :graph/points []))))

(defn update-end-time [st fn]
  (-> st
      (update-in [:navigator/by-id 10600 :end-time] fn)
      (rm-all-points)))

(defn assoc-end-time [st new-val]
  (-> st
      (assoc-in [:navigator/by-id 10600 :end-time] new-val)
      (rm-all-points)))


(defmethod mutate 'navigate/forwards
  [{:keys [state]} _ {:keys [seconds]}]
  {:action #(swap! state update-end-time (fn [end-time] (time/plus end-time (time/seconds seconds))))})

(defmethod mutate 'navigate/backwards
  [{:keys [state]} _ {:keys [seconds]}]
  {:action #(swap! state update-end-time (fn [end-time] (time/minus end-time (time/seconds seconds))))})

(defmethod mutate 'navigate/now
  [{:keys [state]} _ _]
  {:action #(swap! state assoc-end-time (time/now))})


(ns om-alarming.parsing.mutations.navigator
  (:require [om-alarming.reconciler :refer [mutate]]
            [cljs-time.core :as time]))

(defmethod mutate 'navigate/forwards
  [{:keys [state]} _ {:keys [seconds]}]
  {:action #(swap! state update-in [:navigator/by-id 10600 :end-time] (fn [end-time seconds] (time/plus end-time (time/seconds seconds))))})

(defmethod mutate 'navigate/backwards
  [{:keys [state]} _ {:keys [seconds]}]
  {:action #(swap! state update-in [:navigator/by-id 10600 :end-time] (fn [end-time seconds] (time/minus end-time (time/seconds seconds))))})


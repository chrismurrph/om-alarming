(ns om-alarming.parsing.mutations.graph
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]))

(defn update-x-pos [new-x existing-x]
  ;(println "Reting existing " existing-x ", when new is " new-x)
  new-x)

(defn as-mouse-changes [orig-state params]
  (let [new-x (:graph/hover-pos params)
        ;updater (partial update-x-pos new-x)
        ]
    (-> orig-state
        (merge params)
        (assoc-in [:plumb-line/by-id 10201 :x-position] new-x)
        )))

(defmethod mutate 'graph/mouse-change
  [{:keys [state]} _ params]
  {:value  {:keys [:graph/hover-pos :graph/last-mouse-moment :graph/labels-visible?]}
   :action #(swap! state as-mouse-changes params)})

(defmethod mutate 'graph/translators
  [{:keys [state]} _ {:keys [translators]}]
  {:value  {:keys [:graph/translators]}
   :action #(swap! state assoc :graph/translators translators)})

(defmethod mutate 'graph/misc
  [{:keys [state]} _ {:keys [misc]}]
  {:value  {:keys [:graph/misc]}
   :action #(swap! state assoc :graph/misc misc)})

(defmethod mutate 'graph/receiving-chan
  [{:keys [state]} _ {:keys [receiving-chan]}]
  {:value  {:keys [:graph/misc]}
   :action #(swap! state assoc-in [:graph/misc :receiving-chan] receiving-chan)})

(defmethod mutate 'graph/toggle-receive
  [{:keys [state]} _ _]
  {:value  {:keys [[:graph/receiving? _]]}
   :action #(swap! state update-in [:graph/receiving?] not)})

(defmethod mutate 'graph/stop-receive
  [{:keys [state]} _ _]
  {:value  {:keys [[:graph/receiving? _]]}
   :action #(swap! state update-in [:graph/receiving?] false)})

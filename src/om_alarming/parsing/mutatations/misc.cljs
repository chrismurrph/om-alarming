(ns om-alarming.parsing.mutations.misc
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]))

(defmethod mutate 'app/tab
  [{:keys [state]} _ {:keys [new-id]}]
  {:value  {:keys [:app/selected-button]}
   :action #(let [;_ (println "Selected: " new-id)
                  ]
             (swap! state assoc-in [:app/selected-button 1] new-id))})

(defmethod mutate 'graph/mouse-change
  [{:keys [state]} _ params]
  {:value  {:keys [:graph/hover-pos :graph/last-mouse-moment :graph/labels-visible?]}
   :action #(swap! state (fn [old new] (merge old new)) params)})

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

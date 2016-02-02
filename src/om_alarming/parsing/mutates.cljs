(ns om-alarming.parsing.mutates
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
   :action #(swap! state assoc-in [:graph/translators] translators)})

(defmethod mutate 'graph/args
  [{:keys [state]} _ {:keys [args]}]
  {:value  {:keys [:graph/args]}
   :action #(swap! state assoc-in [:graph/args] args)})

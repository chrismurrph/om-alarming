(ns om-alarming.parsing.mutates
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]))

(defmethod mutate 'app/tab
  [{:keys [state]} _ {:keys [new-id]}]
  {:value  {:keys [:app/selected-button]}
   :action #(let [;_ (println "Selected: " new-id)
                  ]
             (swap! state assoc-in [:app/selected-button :id] new-id))})

(defmethod mutate 'graph/hover-pos
  [{:keys [state]} _ {:keys [x]}]
  {:value  {:keys [:graph/hover-pos]}
   :action #(swap! state assoc :graph/hover-pos x)})

(defmethod mutate 'graph/last-mouse-moment
  [{:keys [state]} _ {:keys [now-moment]}]
  {:value  {:keys [:graph/last-mouse-moment]}
   :action #(swap! state assoc :graph/last-mouse-moment now-moment)})

(defmethod mutate 'graph/labels-visible?
  [{:keys [state]} _ {:keys [b]}]
  {:value  {:keys [:graph/labels-visible?]}
   :action #(swap! state assoc :graph/labels-visible? b)})

(defmethod mutate 'graph/translators
  [{:keys [state]} _ {:keys [translators]}]
  {:value  {:keys [:graph/translators]}
   :action #(swap! state assoc-in [:graph/translators] translators)})

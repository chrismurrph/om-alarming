(ns om-alarming.parsing.mutates
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]))

(defmethod mutate 'app/tab
  [{:keys [state]} _ {:keys [new-id]}]
  {                                                         ;:value  {:keys [:app/selected-button]}
   :action #(let [;_ (println "Selected: " new-id)
                  ]
             (swap! state assoc-in [:app/selected-button :id] new-id))})

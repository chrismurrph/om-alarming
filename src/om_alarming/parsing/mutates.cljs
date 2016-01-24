(ns om-alarming.parsing.mutates
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]))

(defmethod mutate 'app/tab
  [{:keys [state]} _ {:keys [new-id]}]
  {:value  {:keys [:app/buttons]}
   :action (fn []
             (let [;_ (println "Selected: " new-id)
                   ]
               (swap! (swap! state update-in [:app/buttons 1] assoc :selected false)
                      update-in [:app/buttons (dec new-id)] assoc :selected true)))})

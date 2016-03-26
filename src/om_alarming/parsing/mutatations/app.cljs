(ns om-alarming.parsing.mutations.app
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]
            [om-alarming.components.log-debug :as ld]
            ))

(def id->route
  {
   1 :app/map
   2 :app/trending 
   3 :app/thresholds
   4 :app/reports
   5 :app/automatic
   6 :app/logs
   7 :app/debug
   }
  )

(defmethod mutate 'app/tab
  [{:keys [state]} k {:keys [new-id]}]
  (ld/log-mutation k)
  {:value  {:keys [:app/selected-button :app/route]}
   :action #(let [route (get id->route new-id)
                  _ (println "Selected: " new-id route)
                  ]
             (swap! state (fn [st] (-> st
                                       (assoc-in [:app/selected-button 1] new-id)
                                       (assoc :app/route route)))))})

;(defmethod mutate 'change/route
;  [{:keys [state]} _ {:keys [route]}]
;  {:value {:keys [:app/route]}
;   :action #(swap! state assoc :app/route route)})
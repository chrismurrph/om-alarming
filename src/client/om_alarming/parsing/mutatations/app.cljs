(ns om-alarming.parsing.mutations.app
  (:require [om.next :as om]
            [untangled.client.mutations :as m]
            [om-alarming.components.log-debug :as ld]
            ))

(def id->route
  {
   1 :app/map
   2 :app/trending 
   3 :app/thresholds
   4 :app/reports
   5 :app/sente
   6 :app/automatic
   7 :app/logs
   8 :app/debug
   }
  )

(defmethod m/mutate 'app/update
  [{:keys [state]} _ {:keys [ident data]}]
  {:action (fn [] (swap! state update-in ident #(merge % data)))})

(defmethod m/mutate 'app/authenticate
  [{:keys [state]} _ {:keys [token]}]
  {:action (fn [] (swap! state update-in [:login-dlg/by-id 10900] #(merge % {:app/authenticated? token})))})

(defn server-load [old-state data]
  (-> old-state
      (update :app/server-info #(merge % data))
      (assoc-in [:login-dlg/by-id 10900 :app/server-state-loaded?] true)))

(defmethod m/mutate 'app/server-info
  [{:keys [state]} _ data]
  {:action (fn [] (swap! state server-load data))})

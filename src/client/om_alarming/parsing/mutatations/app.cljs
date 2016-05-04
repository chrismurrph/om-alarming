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

(defmethod m/mutate 'app/tab
  [{:keys [state]} k {:keys [new-id]}]
  (ld/log-mutation k)
  {;:value  {:keys [:app/selected-button :app/route]}
   :action #(let [route (get id->route new-id)
                  _ (println "Selected: " new-id route)
                  ]
             (swap! state (fn [st] (-> st
                                       (assoc-in [:app/selected-button 1] new-id)
                                       (assoc :app/route route)))))})

;;
;; [(app/store-system {:system-going-fn system/going?
;; :system-start-fn system-start-fn
;; :system-stop-fn system/stop!})]
;; There's already a mutate for graph/misc that does the same thing, but with only the key :misc, with value a map
;;
(comment
  (defmethod m/mutate 'app/store-system
    [{:keys [state]} _ {:keys [system-going-fn system-start-fn system-stop-fn]}]
    {:action (fn [] (swap! state update-in [:misc/by-id 10400]
                           #(merge % {:system-going-fn system-going-fn
                                      :system-start-fn system-start-fn
                                      :system-stop-fn  system-stop-fn})))}))

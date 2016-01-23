(ns om-alarming.parsing.app
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [read]]))

(defmethod read :app/gases
  [{:keys [state query]} key _]
  (let [st @state
        _ (println "In read with:" key "," query ".")
        _ (println "In read with:" (get st key))
        ]
    {:value (om/db->tree query (get st key) st)}))
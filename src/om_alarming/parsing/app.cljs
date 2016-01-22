(ns om-alarming.parsing.app
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [read]]))

(defmethod read :app/gases
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))
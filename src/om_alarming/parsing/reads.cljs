(ns om-alarming.parsing.reads
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [read]]))

(defmethod read :app/gases
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :app/tubes
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :app/buttons
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :app/selected-button
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :default
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In read to ret:" (get st key))
        ]
    {:value (get st key)}))

(defmethod read :tube/gases
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In read with:" key "," query ".")
        ;_ (println "In read with:" (get st key))
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/comms-channel
  [{:keys [state query]} _ _]
  (let [st @state]
    {:value (get-in st [:graph/args :comms])}))

(defmethod read :graph/lines
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

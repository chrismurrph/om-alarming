(ns om-alarming.parsing.reads
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [read my-parser my-reconciler]]))

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

(defmethod read :trending
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "trending RECEIVED query: " query)
        res (om/db->tree query st st)
        ;_ (println "trending query RES: " res)
        ]
    {:value res}))

;;
;; For those that can be nil we can't reply on the default
;;
(defmethod read :graph/hover-pos
  [{:keys [state _]} key _]
  (let [st @state]
    {:value (get st key)}))
(defmethod read :graph/labels-visible?
  [{:keys [state _]} key _]
  (let [st @state]
    {:value (get st key)}))

(defmethod read :graph/lines
  [{:keys [state query]} key _]
  (let [st @state
        _ (println "In :graph/lines for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/points
  [{:keys [state query]} key _]
  (let [st @state
        _ (println "In :graph/points for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/args
  [{:keys [state query]} key _]
  (let [st @state
        _ (println "In :graph/args for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :tube/gases
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In read with:" key "," query ".")
        ;_ (println "In read with:" (get st key))
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :in-sticky-time?
  [{:keys [state _]} key _]
  (let [st @state]
    {:value (get-in st [:graph/plumb-line :in-sticky-time?])}))

(defmethod read :default
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In read to ret:" (get st key))
        res (get st key)
        _ (assert res (str "Nothing found at :default for supposed "
                           "top level key: " key))
        ]
    {:value res}))
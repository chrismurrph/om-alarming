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

(defmethod read :debug
  [{:keys [state query]} key _]
  (let [st @state
        res (om/db->tree query st st)
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
(defmethod read :graph/receiving?
  [{:keys [state _]} key _]
  (let [st @state]
    {:value (get st key)}))

(defmethod read :graph/lines
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In :graph/lines for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/line-from-ident
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In :graph/line-from-ident for:" query)
        ]
    {:value (get-in (get st :graph/lines) query)}))

(defmethod read :graph/x-gas-details
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In :graph/x-gas-details for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/labels
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In :graph/labels for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/drop-info
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In :graph/drop-info for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/plumb-line
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In :graph/plumb-line for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/points
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In :graph/points for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/line-idents
  [{:keys [state _]} key _]
  (let [st @state]
    {:value (get st :graph/lines)}))

(defmethod read :graph/misc
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In :graph/misc for:" query)
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
  (let [st @state
        _ (println "In in-sticky-time? for:" key)]
    {:value (get-in st [:graph/plumb-line 10201 :in-sticky-time?])}))

(defmethod read :receiving-chan
  [{:keys [state _]} key _]
  (let [st @state]
    {:value (get-in st [:graph/misc :receiving-chan])}))

(defmethod read :default
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In read to ret:" (get st key))
        res (get st key)
        _ (assert res (str "Nothing found at :default for supposed "
                           "top level key: " key))
        ]
    {:value res}))
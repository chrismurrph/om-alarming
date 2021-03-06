(ns om-alarming.parsing.reads
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [read my-parser my-reconciler]]
            [om-alarming.util.utils :as u]
            [default-db-format.core :as db-format]))

(defmethod read :app/sys-gases
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :app/login-info
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

(defmethod read :graph/trending-graph
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "trending RECEIVED query: " query)
        res (om/db->tree query (get st key) st)
        ;_ (println "trending query RES: " res)
        ]
    {:value res}))

;(defmethod read :hover-pos
;  [{:keys [state query]} key _]
;  (let [st @state]
;    {:value (om/db->tree query (get st key) st)}))

(defmethod read :debug
  [{:keys [state query]} key _]
  (let [st @state
        res (om/db->tree query st st)
        ]
    {:value res}))

;;
;; For those that can be nil we can't reply on the default
;;
;(defmethod read :graph/labels-visible?
;  [{:keys [state _]} key _]
;  (let [st @state]
;    {:value (get st key)}))

(defmethod read :receiving?
  [{:keys [state _]} key _]
  (let [st @state]
    {:value (get-in st [:navigator/by-id 10600 :receiving?])}))

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

;(defmethod read :graph/drop-info
;  [{:keys [state query]} key _]
;  (let [st @state
;        ;_ (println "In :graph/drop-info for:" query)
;        ]
;    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/plumb-line
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In :graph/plumb-line for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/trending-graph
  [{:keys [state query]} key _]
  (let [st @state
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/navigator
  [{:keys [state query]} key _]
  (let [st @state
        ]
    {:value (om/db->tree query (get st key) st)}))

#_(defmethod read :graph/points
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

(defmethod read :graph/feeder
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/current-line
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In :graph/current-line for:" query)
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :tube/real-gases
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In read with:" key "," query ".")
        ;_ (println "In read with:" (get st key))
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :grid/gas-query-grid
  [{:keys [state query]} key _]
  (let [st @state
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :grid/gas-query-panel
  [{:keys [state query]} key _]
  (let [st @state
        ]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :in-sticky-time?
  [{:keys [state]} key _]
  (let [st @state
        _ (println "In in-sticky-time? for:" key)]
    {:value (get-in st [:plumb-line/by-id 10201 :in-sticky-time?])}))

(defmethod read :route/data
  [{:keys [state query]} k _]
  (let [st @state
        route (get st :app/route)
        route (cond-> route
                      (= (second route) '_) pop)]
    ;; since the route is an `ident`, it could also
    ;; be passed as the second argument to `db->tree`
    ;; if our data was normalized
    {:value (u/probe ":route/data RES:" (get-in st route))}))

(defmethod read :app/route
  [{:keys [state query]} k _]
  (let [st @state]
    {:value (u/probe ":app/route RES:" (get st k))}))

(defmethod read :trending/selected?
  [{:keys [state] :as env} key {:keys [intersect-id]}]
  (let [st @state]
    {:value nil
     ;;
     ;; Was just starting when decided against this idea. Would first need to:
     ;; (get st :graph/lines)
     ;; filter on these idents with get-in returning only where :intersect matches intersect-id
     ;;
     }))

(def hof (db-format/by-id-kw-hof "by-id"))
(defn ident? [v] (db-format/ident? hof v))
(defn vec-of-idents? [v] (db-format/vec-of-idents? hof v))

(defmethod read :default
  [{:keys [state query]} key _]
  (let [st @state
        ;_ (println "In read to ret:" (get st key))
        res (get st key)
        _ (assert res (str "MY ASSERT: Nothing found at :default for supposed "
                           "top level key: " key))
        _ (assert (not (ident? res)) (str "MY ASSERT: Got ident: " res ", for key: " key))
        _ (assert (not (vec-of-idents? res)) (str "MY ASSERT: Got vec-of-idents: " res ", for key: " key))
        ]
    {:value res}))
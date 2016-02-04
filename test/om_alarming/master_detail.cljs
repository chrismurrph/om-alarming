 (ns om-alarming.master-detail
   (:require [om.next :as om :refer-macros [defui]]
             [om.dom :as dom]
             [cljs.pprint :as pp :refer [pprint]]
             [om-alarming.graph.mock-values :refer [pink green blue red]]))

(enable-console-print!)

(def initial-state
  {:graph/drop-info
   {:id            10200 ;; React can use to differentiate
    :x             50
    :lines      [{:id 100} {:id 101} {:id 102} {:id 103}]
    :x-gas-details [{:id 10100} {:id 10101} {:id 10102}]}
   :graph/lines
   [{:id     100
     :intersect {:id 300}
     :name "Methane at 1" :units "%" :colour pink
     :graph/points [{:id 2000}{:id 2001}{:id 2002}]}
    {:id     101
     :intersect {:id 301}
     :name "Oxygen at 1" :units "%" :colour green
     :graph/points [{:id 2003}{:id 2004}{:id 2005}]}
    {:id     102
     :intersect {:id 303}
     :name "Carbon Dioxide at 1" :units "%" :colour blue
     :graph/points [{:id 2006}{:id 2007}{:id 2008}]}
    {:id     103
     :intersect {:id 302}
     :name "Carbon Monoxide at 1" :units "ppm" :colour red
     :graph/points [{:id 2009}{:id 2010}{:id 2011}]}
    ]
   :system-gases
   [{:id 200 :short-name "Methane"}
    {:id 201 :short-name "Oxygen"}
    {:id 202 :short-name "Carbon Monoxide"}
    {:id 203 :short-name "Carbon Dioxide"}]
   :app/tubes
   [{:id 1000 :tube-num "Invercargill"}
    {:id 1001 :tube-num "Dunedin"}]
   :graph/points
   [{:id 2000 :x 10 :y 23}
    {:id 2001 :x 11 :y 24}
    {:id 2002 :x 12 :y 25}
    {:id 2003 :x 13 :y 26}
    {:id 2004 :x 14 :y 27}
    {:id 2005 :x 15 :y 28}
    {:id 2006 :x 16 :y 29}
    {:id 2007 :x 17 :y 30}
    {:id 2008 :x 18 :y 31}
    {:id 2009 :x 19 :y 32}
    {:id 2010 :x 20 :y 33}
    {:id 2011 :x 21 :y 34}]
   })

(def tab #js {:style #js {:marginLeft 20}})
(def double-tab #js {:style #js {:marginLeft 40}})

(defui Point
  static om/Ident
  (ident [this props]
    [:graph-point/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :x :y])
  Object
  (render [this]
    (let [props (om/props this)
          _ (println "Point PROPs: " props)
          {:keys [x y]} props
          _ (println "point: " x y)]
      (dom/div double-tab (str "Point: <" x y ">")))))
(def point-comp (om/factory Point))

(defui Line
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name :units :colour {:graph/points (om/get-query Point)}])
  Object
  (render [this]
    (let [props (om/props this)
          _ (println "Line PROPs: " props)
          {:keys [name units graph/points]} props
          _ (println "name units: " name units)]
      (dom/div tab (str "\tLine: <" name units ">")
               (for [point points]
                 (point-comp point))))))
 (def line-comp (om/factory Line))

;;
;; Not implemented. As these methods do not need to be called this should not matter.
;; Still it is worth visiting http://localhost:3449, and looking in the Dev Tools
;; Console - but expect problems. In fact you need to go there and refresh before
;; calling (show-db).
;;
(defmulti read om/dispatch)
(defmulti mutate om/dispatch)
(def parser
  (om/parser {:read read
              :mutate mutate}))

;(defmethod read :graph/drop-info
;  [{:keys [state query]} key _]
;  (let [st @state
;        _ (println "In :graph/drop-info for:" query)
;        ]
;    {:value (om/db->tree query (get st key) st)}))

;;
;; Too easy to mess up with a default
;;
;(defmethod read :default
;  [{:keys [state query]} key _]
;  (let [st @state
;        ;_ (println "In read to ret:" (get st key))
;        res (get st key)
;        _ (assert res (str "Nothing found in :default for supposed "
;                           "top level key: " key))
;        ]
;    {:value res}))

;;
;; Top level and will never be requested with
;; an Ident so we just grab it
;;
(defmethod read :graph/drop-info
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (get st key)}))

;;
;; An Ident will be coming into props so we need db->tree
;;
(defmethod read :graph/lines
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/points
  [{:keys [state query]} key _]
  (let [st @state
        _ (println "Not done!")]
    {:value (om/db->tree query (get st key) st)}))

(def reconciler
  (om/reconciler {:normalize true ;; Documentation
                  :state initial-state
                  :parser parser}))

(defn show-db
  "(in-ns 'om-alarming/graph-tree-db)
   (show-db)
   from the REPL you started with `lein figwheel test`"
  []
  (pprint @reconciler)
  nil)

(defui Root
  static om/IQuery
  (query [this]
    [:graph/drop-info
     {:graph/lines (om/get-query Line)}])
  Object
  (render [this]
    (show-db)
    (let [props (om/props this)
          ;_ (println "ROOT PROPs: " props)
          {:keys [graph/drop-info graph/lines]} props
          ;_ (println "DROP info: " drop-info)
          ]
      (dom/div nil (str "Drop down at: <" (:x drop-info) ">")
               (dom/div nil
                        (for [line lines]
                          (line-comp line)))))))

(defn run []
  (om/add-root! reconciler
                Root
                (.. js/document (getElementById "main-app-area"))))

(run)



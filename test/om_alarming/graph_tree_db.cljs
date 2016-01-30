(ns om-alarming.graph-tree-db
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pp :refer [pprint]]
            [om-alarming.graph.mock-values :refer [pink green blue red]]))

(comment
  "Idea to create an application with Om Next - start off with denormalized data and
  get components that don't have a render method to do the normalization.")
(def initial-state
  {:graph/lines
   [{:id     100
     :intersect 300
     :name "Methane at 1" :units "%" :colour pink
     :points [{:id 2000}{:id 2001}{:id 2002}]}
    {:id     101
     :intersect 301
     :name "Oxygen at 1" :units "%" :colour green
     :points [{:id 2003}{:id 2004}{:id 2005}]}
    {:id     102
     :intersect 303
     :name "Carbon Dioxide at 1" :units "%" :colour blue
     :points [{:id 2006}{:id 2007}{:id 2008}]}
    {:id     103
     :intersect 302
     :name "Carbon Monoxide at 1" :units "ppm" :colour red
     :points [{:id 2009}{:id 2010}{:id 2011}]}
    ]
   :system-gases
   [{:id 200 :gas-name "Methane"}
    {:id 201 :gas-name "Oxygen"}
    {:id 202 :gas-name "Carbon Monoxide"}
    {:id 203 :gas-name "Carbon Dioxide"}]
   :locations
   [{:id 1000 :location-name "Invercargill"}
    {:id 1001 :location-name "Dunedin"}]
   :location-gases
   [{:id 300 :value 10.1 :location {:id 1000} :system-gas {:id 200}}
    {:id 301 :value 10.2 :location {:id 1000} :system-gas {:id 201}}
    {:id 302 :value 10.3 :location {:id 1000} :system-gas {:id 202}}
    {:id 303 :value 10.4 :location {:id 1000} :system-gas {:id 203}}
    {:id 304 :value 10.5 :location {:id 1001} :system-gas {:id 200}}
    {:id 305 :value 10.6 :location {:id 1001} :system-gas {:id 201}}
    {:id 306 :value 10.7 :location {:id 1001} :system-gas {:id 202}}
    {:id 307 :value 10.8 :location {:id 1001} :system-gas {:id 203}}]
   :points
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

(defui SystemGas
  static om/Ident
  (ident [this props]
    [:gas-of-system/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :gas-name]))

(defui Location
  static om/Ident
  (ident [this props]
    [:location/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :location-name]))

(defui Point
  static om/Ident
  (ident [this props]
    [:point/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :x :y]))

(defui Intersect
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :value {:location (om/get-query Location)} {:system-gas (om/get-query SystemGas)}]))

(defui Line
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :colour {:intersect (om/get-query Intersect)} {:points (om/get-query Point)}]))

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
    [{:system-gases (om/get-query SystemGas)}
     {:locations (om/get-query Location)}
     {:points (om/get-query Point)}
     {:location-gases (om/get-query Intersect)}
     {:graph/lines (om/get-query Line)}])
  ;Object
  ;(render [this]
  ;  (show-db))
  )

(defn run []
  (om/add-root! reconciler
                Root
                (.. js/document (getElementById "main-app-area"))))

(run)

(defn log [& txts]
  (.log js/console (apply str txts)))




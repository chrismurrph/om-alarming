(ns om-alarming.graph-tree-db
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pp :refer [pprint]]
            [om-alarming.util.colours :refer [pink green blue red]]))

(comment
  "Idea to create an application with Om Next - start off with denormalized data and
  get components that don't have a render method to do the normalization.")
(def initial-state
  {:graph/lines
   [{:id     100
     :intersect {:id 300}
     :name "Methane at 1" :units "%" :colour pink
     :points [{:id 2000}{:id 2001}{:id 2002}]}
    {:id     101
     :intersect {:id 301}
     :name "Oxygen at 4" :units "%" :colour green
     :points [{:id 2003}{:id 2004}{:id 2005}]}
    {:id     102
     :intersect {:id 303}
     :name "Carbon Dioxide at 2" :units "%" :colour blue
     :points [{:id 2006}{:id 2007}{:id 2008}]}
    {:id     103
     :intersect {:id 302}
     :name "Carbon Monoxide at 3" :units "ppm" :colour red
     :points [{:id 2009}{:id 2010}{:id 2011}]}
    ]
   :system-gases
   [{:id 200 :short-name "Methane"}
    {:id 201 :short-name "Oxygen"}
    {:id 202 :short-name "Carbon Monoxide"}
    {:id 203 :short-name "Carbon Dioxide"}]
   :app/tubes
   [{:id 1000 :tube-num "Invercargill"}
    {:id 1001 :tube-num "Dunedin"}]
   :tube/real-gases
   [{:id 300 :value 10.1 :tube {:id 1000} :system-gas {:id 200}}
    {:id 301 :value 10.2 :tube {:id 1000} :system-gas {:id 201}}
    {:id 302 :value 10.3 :tube {:id 1000} :system-gas {:id 202}}
    {:id 303 :value 10.4 :tube {:id 1000} :system-gas {:id 203}}
    {:id 304 :value 10.5 :tube {:id 1001} :system-gas {:id 200}}
    {:id 305 :value 10.6 :tube {:id 1001} :system-gas {:id 201}}
    {:id 306 :value 10.7 :tube {:id 1001} :system-gas {:id 202}}
    {:id 307 :value 10.8 :tube {:id 1001} :system-gas {:id 203}}]
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

(defui SystemGas
  static om/Ident
  (ident [this props]
    [:gas-of-system/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :short-name]))

(defui Location
  static om/Ident
  (ident [this props]
    [:tube/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :tube-num]))

(defui Point
  static om/Ident
  (ident [this props]
    [:graph-point/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :x :y]))

(defui Intersect
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :value {:tube (om/get-query Location)} {:system-gas (om/get-query SystemGas)}]))

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
     {:app/tubes (om/get-query Location)}
     {:graph/points (om/get-query Point)}
     {:tube/real-gases (om/get-query Intersect)}
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




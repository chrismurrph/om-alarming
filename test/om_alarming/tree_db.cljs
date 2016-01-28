(ns om-alarming.tree-db
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pp :refer [pprint]]))

(comment
  "Idea to create an application with Om Next - start off with denormalized data and
  get components that don't have a render method to do the normalization.")
(def initial-state
  {:system-gases
   [{:id 200 :gas-name "Methane"}
    {:id 201 :gas-name "Oxygen"}
    {:id 202 :gas-name "Carbon Monoxide"}
    {:id 203 :gas-name "Carbon Dioxide"}]
   :locations
   [{:id 100 :location-name "Invercargill"}
    {:id 101 :location-name "Dunedin"}]
   :location-gases
   [{:id 300 :value 10.1 :location {:id 100} :system-gas {:id 200}}
    {:id 301 :value 10.2 :location {:id 100} :system-gas {:id 201}}
    {:id 302 :value 10.3 :location {:id 100} :system-gas {:id 202}}
    {:id 303 :value 10.4 :location {:id 100} :system-gas {:id 203}}
    {:id 304 :value 10.5 :location {:id 101} :system-gas {:id 200}}
    {:id 305 :value 10.6 :location {:id 101} :system-gas {:id 201}}
    {:id 306 :value 10.7 :location {:id 101} :system-gas {:id 202}}
    {:id 307 :value 10.8 :location {:id 101} :system-gas {:id 203}}]})

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

(defui LocationGasCell
  static om/Ident
  (ident [this props]
    [:location-gas/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :value {:location (om/get-query Location)} {:system-gas (om/get-query SystemGas)}]))

;;
;; A row for the table view. There are 2 locations. Each has 4 gases
;;
(defui LocationRow
  static om/Ident
  (ident [this props]
    [:location/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :location-name {:location-gases (om/get-query LocationGasCell)}]))

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
  "(in-ns 'om-alarming/tree-db)
   (show-db)
   from the REPL you started with `lein figwheel test`"
  []
  (pprint @reconciler)
  nil)

(defui Root
  static om/IQuery
  (query [this]
    [{:system-gases (om/get-query SystemGas)}
     {:locations (om/get-query LocationRow)}
     {:location-gases (om/get-query LocationGasCell)}])
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




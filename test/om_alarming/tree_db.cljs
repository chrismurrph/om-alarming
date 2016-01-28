(ns om-alarming.tree-db
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pp :refer [pprint]]))

(comment
  "My theory of how to create an application with Om Next is to start off with denormalized data and
  get components that don't have a render method to do the normalization for me. If these components can
  do this then hopefully they will be a good set for rendering too. At the moment I
  can get normalization happening for the master-detail relationships, but not for the lookup relationships.
  The actual output is in the next comment...")
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
   [{:id 300 :value 10.1 :location 100 :system-gas 200}
    {:id 301 :value 10.2 :location 100 :system-gas 201}
    {:id 302 :value 10.3 :location 100 :system-gas 202}
    {:id 303 :value 10.4 :location 100 :system-gas 203}
    {:id 304 :value 10.5 :location 101 :system-gas 200}
    {:id 305 :value 10.6 :location 101 :system-gas 201}
    {:id 306 :value 10.7 :location 101 :system-gas 202}
    {:id 307 :value 10.8 :location 101 :system-gas 203}]})

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
    ;;
    ;; These joins are for lookup relationships and unfortunately are not working
    ;;
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

;; Just:
;; (in-ns 'om-alarming/tree-db)
;; (show-db)
;; from the REPL you started with `lein figwheel test`
(defn show-db []
  (pprint @reconciler)
  nil
  )
(comment
  "RESULT HERE"
  {:system-gases
              [[:gas-of-system/by-id 200]
               [:gas-of-system/by-id 201]
               [:gas-of-system/by-id 202]
               [:gas-of-system/by-id 203]],
   :locations [[:location/by-id 100] [:location/by-id 101]],
   :location-gases
              [{:id 300, :value 10.1, :location 100, :system-gas 200}
               {:id 301, :value 10.2, :location 100, :system-gas 201}
               {:id 302, :value 10.3, :location 100, :system-gas 202}
               {:id 303, :value 10.4, :location 100, :system-gas 203}
               {:id 304, :value 10.5, :location 101, :system-gas 200}
               {:id 305, :value 10.6, :location 101, :system-gas 201}
               {:id 306, :value 10.7, :location 101, :system-gas 202}
               {:id 307, :value 10.8, :location 101, :system-gas 203}],
   :gas-of-system/by-id
              {200 {:id 200, :gas-name "Methane"},
               201 {:id 201, :gas-name "Oxygen"},
               202 {:id 202, :gas-name "Carbon Monoxide"},
               203 {:id 203, :gas-name "Carbon Dioxide"}},
   :location/by-id
              {100 {:id 100, :location-name "Invercargill"},
               101 {:id 101, :location-name "Dunedin"}}})

(defui Root
  static om/IQuery
  (query [this]
    [{:system-gases (om/get-query SystemGas)}
     {:locations (om/get-query LocationRow)}])
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




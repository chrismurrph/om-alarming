(ns om-alarming.core
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.reconciler :refer [reconciler]]
            [om-alarming.parsing.reads]
            [om-alarming.utils :as u]
            [om-alarming.components.grid :as grid]
            [om-alarming.components.nav :as nav]
            [cljs.pprint :as pp :refer [pprint]]
            ))

(enable-console-print!)

(defui App
  static om/IQuery
  (query [this]
    [
     {:app/gases (om/get-query grid/SystemGas)}
     {:app/tubes (om/get-query grid/GridRow)}
     {:tube/gases (om/get-query grid/GridDataCell)}
     {:app/buttons (om/get-query nav/TabButton)}
     :app/selected-button
     ])
  Object
  (render [this]
    (let [props (om/props this)
          ;_ (pprint @reconciler)
          ;{:keys [app/gases app/tubes]} props
          ]
      (dom/div nil
               (let [buttons-props (select-keys props [:app/buttons :app/selected-button])]
                 (nav/menubar buttons-props))
               (grid/gas-selection-grid props)
               ;(let [grid-props (select-keys props [:app/gases :app/tubes])]
               ;  (gas-selection-grid grid-props)
               ;  ;(dom/div nil
               ;  ;         (gas-selection-grid grid-props)
               ;  ;         ;(dom/h4 nil (str "gases are " (map :gas (:app/gases grid-props))))
               ;  ;         ;(dom/h4 nil (str "tubes are " (:app/tubes grid-props)))
               ;  ;         )
               ;  )
               )
      )))

(defn run []
  (om/add-root! reconciler
                App
                (.. js/document (getElementById "main-app-area"))))

(run)

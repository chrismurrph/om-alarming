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
            ))

(enable-console-print!)

;;
;; Need an Ident for db->query to work. These are just the gases themselves, so there might only be 4 of them
;;
(defui Gas
  static om/Ident
  (ident [this props]
    [:gas-of-system/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :gas]))

(defui App
  static om/IQuery
  (query [this]
    [{:app/gases (om/get-query Gas)}
     {:app/tubes (om/get-query grid/GridRow)}
     {:app/buttons (om/get-query nav/TabButton)}
     :app/selected-button
     ])
  Object
  (render [this]
    (let [props (om/props this)
          ;{:keys [app/gases app/tubes]} props
          ]
      (dom/div nil
               (let [buttons-props (select-keys props [:app/buttons :app/selected-button])]
                 (nav/menubar buttons-props))
               (let [grid-props (select-keys props [:app/gases :app/tubes])]
                 ;(grid/gas-selection-grid grid-props)
                 (dom/div nil
                          (dom/h4 nil (str "gases are " (map :gas (:app/gases grid-props))))
                          (dom/h4 nil (str "tubes are " (:app/tubes grid-props)))
                          )
                 )
               )
      )))

(defn run []
  (om/add-root! reconciler
                App
                (.. js/document (getElementById "main-app-area"))))

(run)

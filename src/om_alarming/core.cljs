(ns om-alarming.core
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.reconciler :refer [reconciler]]
            [om-alarming.parsing.all :as app]
            [om-alarming.utils :as u]
            [om-alarming.components.grid :as grid]
            [om-alarming.components.nav :as nav]
            ))

(enable-console-print!)

;;
;; Need an Ident for db->query to work
;;
(defui Gas
  static om/Ident
  (ident [this props]
    [:gas/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name]))

(defui App
  static om/IQuery
  (query [this]
    [{:app/gases (om/get-query Gas)}
     {:app/tubes (om/get-query grid/GridRow)}
     {:app/buttons (om/get-query nav/TabButton)}
     ])
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [app/gases app/tubes]} props]
      (dom/div nil
               (let [buttons-props (select-keys props [:app/buttons])]
                 (nav/menubar buttons-props))
               (dom/h3 nil (str "gases are " (map :name gases)))
               (dom/h3 nil (str "tubes are " tubes)))
      )))

(defn run []
  (om/add-root! reconciler
                App
                (.. js/document (getElementById "main-app-area"))))

(run)

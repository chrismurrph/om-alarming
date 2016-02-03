(ns om-alarming.core
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.reconciler :refer [my-reconciler]]
            [om-alarming.parsing.reads]
            [om-alarming.utils :as u]
            [om-alarming.components.grid :as grid]
            [om-alarming.components.general :as gen]
            [om-alarming.components.nav :as nav]
            [om-alarming.components.graphing :as graph]
            [om-alarming.graph.processing :as p]
            [cljs.pprint :as pp :refer [pprint]]
            ))

(enable-console-print!)

(defui App
  static om/IQuery
  (query [this]
    [
     {:app/gases (om/get-query gen/SystemGas)}
     {:app/tubes (om/get-query grid/GridRow)}
     {:tube/gases (om/get-query grid/GridDataCell)}
     {:app/buttons (om/get-query nav/TabButton)}
     {:app/selected-button (om/get-query nav/TabButton)}
     {:graph/points (om/get-query graph/Point)}
     {:graph/lines (om/get-query graph/Line)}
     {:graph/x-gas-details (om/get-query graph/RectTextTick)}
     {:graph/drop-info (om/get-query graph/DropInfo)}
     {:graph/labels (om/get-query graph/Label)}
     {:graph/init [:width :height]}
     :graph/comms-channel
     {:graph/plumb-line (om/get-query graph/PlumbLine)}
     ])
  Object
  (render [this]
    (let [app-props (om/props this)
          ;_ (pprint @my-reconciler)
          ]
      (dom/div nil
               (let []
                 )
               (nav/menu-bar (:app/buttons app-props) (:app/selected-button app-props))
               (let [selected (:name (:app/selected-button app-props))]
                 (case selected
                   "Map" (dom/div nil "Nufin")
                   "Trending" (grid/gas-query-panel app-props)
                   "Thresholds" (dom/div nil "Nufin")
                   "Reports" (dom/div nil "Nufin")
                   "Automatic" (dom/div nil "Nufin")
                   "Logs" (dom/div nil "Nufin")
                   ))))))


(defn run []
  (om/add-root! my-reconciler
                App
                (.. js/document (getElementById "main-app-area")))
  (p/init))
(run)

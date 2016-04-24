(ns om-alarming.ui
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [om-alarming.components.graphing :as graph]
            [om-alarming.components.grid :as grid]
            [om-alarming.components.general :as gen]
            [om-alarming.components.log-debug :as ld]
            [om-alarming.components.navigator :as navigator]))

(defui ^:once MapTab
  static om/IQuery
  (query [this] [:id :tab/type :tab/label])
  Object
  (render [this]
    (let [{:keys [tab/label]} (om/props this)]
      (dom/div nil
               (dom/h3 nil label)
               (dom/p nil "map ...")))))
(def ui-map-tab (om/factory MapTab))

;;
;; Replacement for GasQueryPanel, which we still need to transfer contents across
;;
(defui ^:once TrendingTab
  static om/IQuery
  (query [this] [:id
                 :tab/type
                 :tab/label
                 {:grid/gas-query-grid (om/get-query grid/GasQueryGrid)}
                 {:graph/trending-graph (om/get-query graph/TrendingGraph)}
                 ])
  Object
  (render [this]
    (ld/log-render "TrendingTab" this)
    (let [app-props (om/props this)
          {:keys [grid/gas-query-grid graph/trending-graph]} app-props
          {:keys [click-cb-fn]} (om/get-computed this)
          _ (assert click-cb-fn "gas-query-panel")
          grid-row-computed-map {:click-cb-fn click-cb-fn}
          _ (assert gas-query-grid (str "Don't yet have gas-query-grid in: " (keys app-props)))
          ]
      (dom/div nil #_{:className "ui three column internally celled grid container"}
               ;;
               ;; grid and trending graph need to be made separate. User dragging the mouse around s/not mean
               ;; that new grid cells are created. Anything related to the plumb line moving can go into local state.
               ;; When it stops moving we can copy everything to the plumb line that's in the state.
               ;;
               ;"Gas Query Panel"
               (dom/div #js {:className "side"}
                        (grid/gas-query-grid-component (om/computed gas-query-grid grid-row-computed-map)))
               (dom/div #js {:className "side"}
                        (graph/trending-graph-component trending-graph))))))
(def ui-trending-tab (om/factory TrendingTab))

(defui ^:once ThresholdsTab
  static om/IQuery
  (query [this] [:id :tab/type :tab/label])
  Object
  (render [this]
    (let [{:keys [tab/label]} (om/props this)]
      (dom/div nil
               (dom/h3 nil label)
               (dom/p nil "thresholds ...")))))
(def ui-thresholds-tab (om/factory ThresholdsTab))

(defui ^:once ReportsTab
  static om/IQuery
  (query [this] [:id :tab/type :tab/label])
  Object
  (render [this]
    (let [{:keys [tab/label]} (om/props this)]
      (dom/div nil
               (dom/h3 nil label)
               (dom/p nil "reports ...")))))
(def ui-reports-tab (om/factory ReportsTab))

(defui ^:once TabUnion
  static om/IQuery
  (query [this] {:app/map        (om/get-query MapTab)
                 :app/trending   (om/get-query TrendingTab)
                 :app/thresholds (om/get-query ThresholdsTab)
                 :app/reports    (om/get-query ReportsTab)})
  static om/Ident
  (ident [this props] [(:tab/type props) (:id props)])
  Object
  (render [this]
    (let [{:keys [tab/type] :as props} (om/props this)
          {:keys [click-cb-fn]} (om/get-computed this)
          _ (assert type)]
      (case type
        :app/map (ui-map-tab props)
        :app/trending (ui-trending-tab (om/computed props {:click-cb-fn click-cb-fn}))
        :app/thresholds (ui-thresholds-tab props)
        :app/reports (ui-reports-tab props)
        (dom/div nil (str "MISSING TAB: <" type ">"))))))
(def ui-tab (om/factory TabUnion))


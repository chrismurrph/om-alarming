(ns om-alarming.components.grid
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util.util :refer [class-names]]
            [om-alarming.components.graphing :as graph]
            [om-alarming.components.general :as gen]
            [om-alarming.parsing.mutations.lines]
            [om-alarming.components.log-debug :as ld]
            [om-alarming.util.utils :as u]
            [cljs.pprint :as pp :refer [pprint]]))

(comment
  (dom/div #js {:className (str "ui" (if selected? " checked " " ") "checkbox")}
           (dom/input #js {:type    "checkbox"
                           :checked selected?
                           :onClick (fn [e] #_(.preventDefault e) (pick-fn))})
           (dom/label nil "")))

(defui GridDataCell
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:grid-cell/id props)])
  static om/IQuery
  (query [this]
    [:grid-cell/id
     {:system-gas (om/get-query gen/SystemGas)}
     {:tube (om/get-query gen/Location)}])
  Object
  (pick [this click-cb-fn id selected?]
    (assert id)
    (assert (not (nil? selected?)))
    (click-cb-fn id selected?))
  (render [this]
    (ld/log-render "GridDataCell" this :grid-cell/id)
    (let [{:keys [grid-cell/id system-gas tube] :as props} (om/props this)
          _ (assert id)
          {:keys [tube-num sui-col-info click-cb-fn selected?]} (om/get-computed this)
          _ (assert sui-col-info)
          _ (assert click-cb-fn "GridDataCell")
          ]
      (if system-gas
        (dom/div sui-col-info
                 (dom/div #js {:className (str "ui" (if selected? " checked " " ") "checkbox")}
                          (dom/input #js {:type    "checkbox"
                                          :checked (boolean selected?) ;; <- Note boolean function - js needs it!
                                          :onClick (fn [_] (.pick this click-cb-fn id selected?))})
                          (dom/label nil ""))
                 )
        (dom/div sui-col-info
                 (dom/label nil tube-num))))))
(def grid-data-cell-component (om/factory GridDataCell {:keyfn :grid-cell/id}))

(defn selected?
  "Is the gas, which is really gas at location (intersect), one of the ones that there's a line for?"
  [gas line-intersect-ids]
  (let [intersect-id (:grid-cell/id gas) ;; a gas id is the same as an intersect id
        res (some #{intersect-id} line-intersect-ids)
        ;_ (println "Going thru " line-intersect-ids " looking for intersect id " intersect-id " got " res)
        ]
    res))

#_(defui GridRow
  static om/Ident
  (ident [this props]
    [:tube/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     :tube-num
     {:tube/real-gases (om/get-query GridDataCell)}])
  Object
  (render [this]
    (ld/log-render "GridRow" this)
    (let [{:keys [id tube-num tube/real-gases]} (om/props this)
          computed-props (om/get-computed this)
          {:keys [lines-intersect-ids pick-colour-fn sui-col-info]} computed-props
          _ (assert lines-intersect-ids)
          _ (assert pick-colour-fn (str "GridRow: " computed-props))
          _ (assert sui-col-info)
          ;lines-intersect-ids (map #(-> % :intersect :id) lines)
          ;_ (println "gases: " gases)
          ;_ (println "lines: " lines)
          hdr-and-gases (into [{:grid-cell/id 0}] real-gases)
          ]
      (dom/div #js {:className "row"}
               (for [gas hdr-and-gases]
                 (grid-data-cell (om/computed gas
                                              (merge
                                                {:tube-num tube-num}
                                                {:selected? (selected? gas lines-intersect-ids)}
                                                computed-props))))))))
#_(def grid-row (om/factory GridRow {:keyfn :id}))

;;
;; Another way of looking at a gas but don't need an ident as already have with SystemGas
;;
(defui GridHeaderLabel
  static om/IQuery
  (query [this]
    [:id
     :short-name])
  Object
  (render [this]
    (ld/log-render "GridHeaderLabel" this)
    (let [props (om/props this)
          {:keys [short-name]} props
          {:keys [sui-col-info]} (om/get-computed this)
          ;_ (println "GAS:" name)
          ]
      (dom/div sui-col-info
               (dom/label nil short-name)))))
(def grid-header-label (om/factory GridHeaderLabel {:keyfn :id}))

(defui GridHeaderRow
  Object
  (render [this]
    (ld/log-render "GridHeaderRow" this)
    (let [sys-gases (:app/sys-gases (om/props this))
          hdr-gases (into [{:id 0 :short-name "Tube"}] sys-gases)]
      (dom/div #js {:className "row"}
               (map #(grid-header-label (om/computed % (om/get-computed this))) hdr-gases)))))
(def grid-header-row (om/factory GridHeaderRow {:keyfn (fn [_] "GridHeaderRow")}))

#_(defn grid-body-row [map-fn hdr-and-gases]
  (dom/div #js {:className "row"}
           (map map-fn hdr-and-gases)))

;;
;; One we are completely making up, for React's benefit
;;
(defui GridBodyRow
  Object
  (render [this]
    (ld/log-render "GridBodyRow" this)
    (let [{:keys [id real-gases]} (om/props this)
          _ (assert id)
          _ (assert real-gases)
          {:keys [sui-col-info click-cb-fn lines]} (om/get-computed this)
          _ (assert (and sui-col-info click-cb-fn))
          _ (assert lines)]
      (dom/div #js {:className "row"}
               (for [gas real-gases]
                 (grid-data-cell-component
                   (om/computed gas {:sui-col-info sui-col-info
                                     :tube-num    (-> gas :hdr-tube-num)
                                     :click-cb-fn click-cb-fn
                                     :selected?   (boolean (some #{gas} (map :intersect lines)))})))))))
(def grid-body-row (om/factory GridBodyRow {:keyfn :id}))

(defui GasQueryGrid
  static om/Ident
  (ident [this props]
    [:gas-query-grid/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     {:tube/real-gases (om/get-query GridDataCell)}
     ; When the user selects or de-selects a line we don't want to be having to update this component in
     ; the mutation, hence passing in by computed.
     ;{:graph/lines (om/get-query graph/Line)}
     {:app/sys-gases (om/get-query gen/SystemGas)}
     ])
  Object
  (render [this]
    (ld/log-render-on "GasQueryGrid" this)
    (let [props (om/props this)
          {:keys [id tube/real-gases app/sys-gases]} props
          _ (assert id)
          _ (assert real-gases "no real-gases inside GasQueryGrid")
          _ (println "FIRST:" (first real-gases))
          ;_ (pprint real-gases)
          {:keys [sui-col-info click-cb-fn lines]} (om/get-computed this)
          _ (assert lines "no lines inside GasQueryGrid")
          sui-col-info-map {:sui-col-info sui-col-info}
          _ (assert sui-col-info)
          sui-grid-info #js {:className "ui column grid"}
          all-hdr-and-gases (map-indexed #(into [{:grid-cell/id 0 :hdr-tube-num (inc %1)}] %2) (partition (count sys-gases) real-gases))]
      (dom/div sui-grid-info
               (grid-header-row (om/computed props sui-col-info-map))
               (for [hdr-and-gases all-hdr-and-gases]
                 (grid-body-row (om/computed {:real-gases hdr-and-gases
                                              :id         (:hdr-tube-num (first hdr-and-gases))}
                                             (merge sui-col-info-map
                                                    {:click-cb-fn click-cb-fn
                                                     :lines lines}))))))))
(def gas-query-grid-component (om/factory GasQueryGrid {:keyfn :id}))

(defui GasQueryPanel
  static om/Ident
  (ident [this props]
    [:gas-query-panel/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     {:grid/gas-query-grid (om/get-query GasQueryGrid)}
     {:graph/trending-graph (om/get-query graph/TrendingGraph)}
     ])
  Object
  (render [this]
    (ld/log-render "GasQueryPanel" this)
    (let [app-props (om/props this)
          {:keys [grid/gas-query-grid graph/trending-graph]} app-props
          {:keys [click-cb-fn lines]} (om/get-computed this)
          sui-col-info-map {:sui-col-info #js {:className "two wide column center aligned"}}
          _ (assert click-cb-fn "gas-query-panel")
          grid-row-computed (merge sui-col-info-map {:click-cb-fn click-cb-fn :lines lines})
          ;_ (assert (:app/sys-gases app-props))
          ;_ (assert (:app/tubes app-props))
          _ (assert trending-graph app-props)
          ;_ (assert (:graph/navigator app-props))
          ;_ (println (:graph/trending-graph app-props))
          ]
      (dom/div #js {:className "ui three column internally celled grid container"}
               ;;
               ;; grid and trending graph need to be made separate. User dragging the mouse around s/not mean
               ;; that new grid cells are created. Anything related to the plumb line moving can go into local state.
               ;; When it stops moving we can copy everything to the plumb line that's in the state.
               ;;
               (dom/div #js {:className "column"}
                        (gas-query-grid-component (om/computed gas-query-grid grid-row-computed)))
               (dom/div #js {:className "two wide column"}
                        (graph/trending-graph-component trending-graph))))))
(def gas-query-panel-component (om/factory GasQueryPanel {:keyfn :id}))

#_(defn gas-query-panel [app-props pick-colour-fn]
  (let [sui-col-info-map {:sui-col-info #js {:className "two wide column center aligned"}}
        _ (assert pick-colour-fn, "gas-query-panel")
        grid-row-computed (merge sui-col-info-map {:pick-colour-fn pick-colour-fn})
        _ (assert (:app/sys-gases app-props))
        _ (assert (:app/tubes app-props))
        _ (assert (:graph/trending-graph app-props))
        _ (assert (:graph/navigator app-props))
        ;_ (println (:graph/trending-graph app-props))
        ]
    (dom/div #js {:className "ui three column internally celled grid container"}
             ;;
             ;; grid and trending graph need to be made separate. User dragging the mouse around s/not mean
             ;; that new grid cells are created. Anything related to the plumb line moving can go into local state.
             ;; When it stops moving we can copy everything to the plumb line that's in the state.
             ;;
             (dom/div #js {:className "column"}
                      (gas-query-grid (om/computed app-props grid-row-computed)))
             (dom/div #js {:className "two wide column"}
                      (graph/trending-graph (:graph/trending-graph app-props))))))

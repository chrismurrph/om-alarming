(ns om-alarming.components.grid
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util.util :refer [class-names]]
            [om-alarming.components.graphing :as graph]
            [om-alarming.components.general :as gen]
            [om-alarming.parsing.mutations.lines]
            [om-alarming.components.log-debug :as ld]
            [om-alarming.util.utils :as u]))

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
  (pick [this click-cb-fn pick-colour-fn id selected?]
    (assert id)
    (assert (not (nil? selected?)))
    (click-cb-fn pick-colour-fn id selected?))
  (render [this]
    (ld/log-render "GridDataCell" this :grid-cell/id)
    (let [{:keys [grid-cell/id system-gas tube] :as props} (om/props this)
          ;_ (println "PROPs" props)
          {:keys [tube-num sui-col-info pick-colour-fn click-cb-fn selected?]} (om/get-computed this)
          _ (assert sui-col-info)
          _ (assert pick-colour-fn "GridDataCell")
          _ (assert click-cb-fn "GridDataCell")
          ;;TODO - we really need to get rid of this happening for no reason, which is an Om Next problem
          ;_ (println "grid cell created, selected: " selected?)
          ]
      (if system-gas
        (dom/div sui-col-info
                 #_(checkbox (om/computed {} (merge props {:selected? selected? :pick-fn #(.pick this pick-colour-fn id selected?)})))
                 (dom/div #js {:className (str "ui" (if selected? " checked " " ") "checkbox")}
                          (dom/input #js {:type    "checkbox"
                                          :checked (boolean selected?) ;; <- Note boolean function - js needs it!
                                          :onClick (fn [_] (.pick this click-cb-fn pick-colour-fn id selected?))})
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

(defui GasQueryGrid
  static om/Ident
  (ident [this props]
    [:gas-query-grid/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     {:tube/real-gases (om/get-query GridDataCell)}
     {:graph/lines (om/get-query graph/Line)}
     ;{:app/tubes (om/get-query gen/Location)}
     ])
  Object
  (render [this]
    (ld/log-render "GasQueryGrid" this)
    (let [props (om/props this)
          {:keys [tube/real-gases graph/lines]} props
          _ (assert real-gases "no real-gases inside GasQueryGrid")
          ;Send in via computed when need it
          ;lines-intersect-ids (map #(-> % :intersect :grid-cell/id) lines)
          {:keys [sui-col-info pick-colour-fn click-cb-fn]} (om/get-computed this)
          sui-col-info-map {:sui-col-info sui-col-info :pick-colour-fn pick-colour-fn :click-cb-fn click-cb-fn}
          for-gas-fn (fn [gas] (grid-data-cell-component (om/computed gas (merge sui-col-info-map {:selected? (boolean (some #{gas} (map :intersect lines)))}))))
          _ (assert sui-col-info)
          sui-grid-info #js {:className "ui column grid"}
          hdr-and-gases (into [{:grid-cell/id 0}] (take 4 real-gases))]
      (dom/div sui-grid-info
               (grid-header-row (om/computed props sui-col-info-map))
               (map for-gas-fn hdr-and-gases)))))
(def gas-query-grid-component (om/factory GasQueryGrid {:keyfn :id}))

(defui GasQueryPanel
  Object
  (render [this]
    (ld/log-render "GasQueryPanel" this)
    (let [app-props (om/props this)
          {:keys [grid/gas-query-grid graph/trending-graph]} app-props
          {:keys [pick-colour-fn click-cb-fn]} (om/get-computed this)
          sui-col-info-map {:sui-col-info #js {:className "two wide column center aligned"}}
          _ (assert pick-colour-fn "gas-query-panel")
          _ (assert click-cb-fn "gas-query-panel")
          grid-row-computed (merge sui-col-info-map {:pick-colour-fn pick-colour-fn :click-cb-fn click-cb-fn})
          ;_ (assert (:app/sys-gases app-props))
          ;_ (assert (:app/tubes app-props))
          _ (assert (:graph/trending-graph app-props) app-props)
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
                        (gas-query-grid-component (om/computed app-props grid-row-computed)))
               #_(dom/div #js {:className "two wide column"}
                        (graph/trending-graph (:graph/trending-graph app-props)))))))
(def gas-query-panel-component (om/factory GasQueryPanel {:keyfn (fn [_] "GasQueryPanel")}))

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

(ns om-alarming.components.grid
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util.util :refer [class-names]]
            [om-alarming.components.graphing :as graph]
            [om-alarming.components.general :as gen]
            [om-alarming.parsing.mutations.lines]))

;;
;; Because there's no query or ident, everything comes in in props
;;
(defui CheckBox
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [test-props pick-colour-fn]} props
          selected (or (:selected props) (:selected test-props))
          ]
      (dom/div #js {:className (str "ui" (if selected " checked " " ") "checkbox")}
               (dom/input #js {:type    "checkbox"
                               :checked (when selected " ")
                               :onClick #(pick-colour-fn)})
               (dom/label nil "")))))
(def checkbox (om/factory CheckBox {:keyfn :id}))

(defui GridDataCell
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:id props)])
  static om/IQueryParams
  (params [this]
    {:intersect-id nil})
  static om/IQuery
  (query [this]
    [:id
     {:system-gas (om/get-query gen/SystemGas)}
     {:tube (om/get-query gen/Location)}
     '[(:trending/selected? {:intersect-id ?intersect-id})]])
  Object
  (pick-colour [this pick-fn id]
    (om/transact! this `[(graph/add-line {:graph-ident [:trending-graph/by-id 10300] :intersect-id ~id :colour ~(pick-fn)})]))
  (render [this]
    (let [{:keys [id system-gas tube] :as props} (om/props this)
          ;_ (println "PROPs" props)
          {:keys [tube-num sui-col-info pick-colour-fn]} (om/get-computed this)
          ]
      (om/set-query! this {:params {:intersect-id id}})
      (if system-gas
        (dom/div sui-col-info
                 (checkbox (merge props {:pick-colour-fn #(.pick-colour this pick-colour-fn id)})))
        (dom/div sui-col-info
                 (dom/label nil tube-num))))))
(def grid-data-cell (om/factory GridDataCell {:keyfn :id}))

(defui GridRow
  static om/Ident
  (ident [this props]
    [:tube/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     :tube-num
     {:tube/gases (om/get-query GridDataCell)}])
  Object
  (render [this]
    (let [{:keys [id tube-num tube/gases]} (om/props this)
          ;{:keys [sui-col-info]} (om/get-computed this)
          ;_ (println "gases: " gases)
          hdr-and-gases (into [{:id 0}] gases)
          ]
      (dom/div #js {:className "row"}
               (for [gas hdr-and-gases]
                 (grid-data-cell (om/computed gas (merge {:tube-num tube-num} (om/get-computed this)))))))))
(def grid-row (om/factory GridRow {:keyfn :id}))

(defui GridHeaderLabel
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [short-name]} props
          {:keys [sui-col-info]} (om/get-computed this)
          ;_ (println "GAS:" name)
          ]
      (dom/div sui-col-info
               (dom/label nil short-name)))))
(def grid-header-label (om/factory GridHeaderLabel {:keyfn :id}))

(defui GridHeaderRow
  ;static om/IQuery
  ;(query [this]
  ;  [:id :app/gases])
  Object
  (render [this]
    (let [{:keys [app/gases]} (om/props this)
          hdr-gases (into [{:id 0 :short-name "Tube"}] gases)]
      (dom/div #js {:className "row"}
               (for [gas hdr-gases]
                 (grid-header-label (om/computed gas (om/get-computed this))))))))
(def grid-header-row (om/factory GridHeaderRow {:keyfn :id}))

(defn gas-query-panel [app-props pick-colour-fn]
  (let [sui-col-info-map {:sui-col-info #js {:className "two wide column center aligned"}}
        grid-row-computed (merge sui-col-info-map {:pick-colour-fn pick-colour-fn})
        sui-grid-info #js {:className "ui column grid"}
        _ (assert (:app/gases app-props))
        _ (assert (:app/tubes app-props))
        _ (assert (:graph/trending-graph app-props))
        _ (assert (:graph/navigator app-props))
        ;_ (println (:graph/trending-graph app-props))
        ]
    (dom/div #js {:className "ui three column internally celled grid container"}
             (dom/div #js {:className "column"}
                      (dom/div sui-grid-info
                               (grid-header-row (om/computed (select-keys app-props [:app/gases]) sui-col-info-map))
                               (for [tube (:app/tubes app-props)]
                                 (grid-row (om/computed tube grid-row-computed)))))
             (dom/div #js {:className "two wide column"}
                      (graph/trending-graph (:graph/trending-graph app-props))))))

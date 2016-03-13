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
          {:keys [test-props pick-fn]} props
          selected? (or (:selected? props) (:selected? test-props))
          ]
      (dom/div #js {:className (str "ui" (if selected? " checked " " ") "checkbox")}
               (dom/input #js {:type    "checkbox"
                               :checked (when selected? " ")
                               :onClick #(pick-fn)})
               (dom/label nil "")))))
(def checkbox (om/factory CheckBox {:keyfn :id}))

(defui GridDataCell
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     {:system-gas (om/get-query gen/SystemGas)}
     {:tube (om/get-query gen/Location)}])
  Object
  (pick [this pick-colour-fn id selected?]
    (if selected?
      (om/transact! this `[(graph/remove-line {:graph-ident [:trending-graph/by-id 10300] :intersect-id ~id}) :app/gases])
      (om/transact! this `[(graph/add-line {:graph-ident [:trending-graph/by-id 10300] :intersect-id ~id :colour ~(pick-colour-fn)})])))
  (render [this]
    (let [{:keys [id system-gas tube] :as props} (om/props this)
          ;_ (println "PROPs" props)
          {:keys [tube-num sui-col-info pick-colour-fn selected?]} (om/get-computed this)
          _ (assert pick-colour-fn)
          ]
      (if system-gas
        (dom/div sui-col-info
                 (checkbox (merge props {:selected? selected? :pick-fn #(.pick this pick-colour-fn id selected?)})))
        (dom/div sui-col-info
                 (dom/label nil tube-num))))))
(def grid-data-cell (om/factory GridDataCell {:keyfn :id}))

(defn selected?
  "Is the gas, which is really gas at location (intersect), one of the ones that there's a line for?"
  [gas line-intersect-ids]
  (let [intersect-id (:id gas) ;; a gas id is the same as an intersect id
        res (some #{intersect-id} line-intersect-ids)
        ;_ (println "Going thru " line-intersect-ids " looking for intersect id " intersect-id " got " res)
        ]
    res))

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
          {:keys [graph/lines]} (om/get-computed this)
          lines-intersect-ids (map #(-> % :intersect :id) lines)
          ;_ (println "gases: " gases)
          ;_ (println "lines: " lines)
          hdr-and-gases (into [{:id 0}] gases)
          ]
      (dom/div #js {:className "row"}
               (for [gas hdr-and-gases]
                 (grid-data-cell (om/computed gas (merge {:tube-num tube-num} {:selected? (selected? gas lines-intersect-ids)} (om/get-computed this)))))))))
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
        grid-row-computed (merge sui-col-info-map {:pick-colour-fn pick-colour-fn} (select-keys app-props [:graph/lines]))
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

(ns om-alarming.components.grid
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names]]
            [om-alarming.business :as bus]))

(defui CheckBox
  Object
  (render [this]
    (let [props (om/props this)
          test-props (:test-props props)
          selected (or (:selected props) (:selected test-props))
          ]
      (dom/div #js {:className (str "ui" (if selected " checked " " ") "checkbox")}
               (dom/input #js {:type "checkbox" :checked (when selected " ")})
               (dom/label nil "")))))

(def checkbox (om/factory CheckBox {:keyfn :id}))

;;
;; Need an Ident for db->query to work. These are just the gases themselves, so there might only be 4 of them
;;
(defui SystemGas
  static om/Ident
  (ident [this props]
    [:gas-of-system/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :short-name]))

(defui GridDataCell
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :selected {:system-gas (om/get-query SystemGas)}])
  Object
  (render [this]
    (let [{:keys [id system-gas] :as props} (om/props this)
          {:keys [tube-num sui-col-info]} (om/get-computed this)
          ;sui-col-info #js {:className "two wide column center aligned"}
          ]
      (if system-gas
        (let [gas-name (:name system-gas)
              full-name (str "Tube " tube-num " " gas-name)]
          (dom/div sui-col-info
                   (checkbox (om/computed props {:full-name full-name}))))
        (dom/div sui-col-info
                 (dom/label nil tube-num))))))

(def grid-data-cell (om/factory GridDataCell {:keyfn :id}))

(defui GridRow
  static om/Ident
  (ident [this props]
    [:tube/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :tube-num {:tube/gases (om/get-query GridDataCell)}])
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
  Object
  (render [this]
    (let [{:keys [app/gases]} (om/props this)
          hdr-gases (into [{:id 0 :short-name "Tube"}] gases)]
      (dom/div #js {:className "row"}
               (for [gas hdr-gases]
                 (grid-header-label (om/computed gas (om/get-computed this))))))))

(def grid-header-row (om/factory GridHeaderRow {:keyfn :id}))

(defn gas-selection-grid [grid-props]
  (let [sui-col-info-map {:sui-col-info #js {:className "one wide column center aligned"}}
        sui-grid-info #js {:className "ui five column grid"}]
    (dom/div sui-grid-info
             (grid-header-row (om/computed (select-keys grid-props [:app/gases]) sui-col-info-map))
             (for [tube (:app/tubes grid-props)]
               (grid-row (om/computed tube sui-col-info-map))))))

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
    [:id :name]))

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
          ;_ (assert system-gas (str "GridDataCell needs to be given a gas keyword, props: " props))
          {:keys [tube-num]} (om/get-computed this)
          ]
      (if system-gas
        (let [gas-name (:name system-gas)
              full-name (str "Tube " tube-num " " gas-name)]
          (dom/div #js {:className "three wide column center aligned"}
                   (checkbox (om/computed props {:full-name full-name}))))
        (dom/div #js {:className "three wide column center aligned"}
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
          _ (println "gases: " gases)
          hdr-and-gases (into [{:id 0}] gases)
          ]
      (dom/div #js {:className "row"}
               (for [gas hdr-and-gases]
                 (grid-data-cell (om/computed gas {:tube-num tube-num})))))))

(def grid-row (om/factory GridRow {:keyfn :id}))

(defui GridHeaderLabel
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [name]} props
          ;_ (println "GAS:" name)
          ]
      (dom/div #js {:className "three wide column center aligned"}
               (dom/label nil name)))))

(def grid-header-label (om/factory GridHeaderLabel {:keyfn :id}))

(defui GridHeaderRow
  Object
  (render [this]
    (let [{:keys [app/gases]} (om/props this)
          hdr-gases (into [{:id 0 :name "Tube"}] gases)]
      (dom/div #js {:className "row"}
               (for [gas hdr-gases]
                 (grid-header-label gas))))))

(def grid-header-row (om/factory GridHeaderRow {:keyfn :id}))

(defn gas-selection-grid [grid-props]
  (dom/div #js {:className "ui five column grid"}
           (grid-header-row (select-keys grid-props [:app/gases]))
           (for [tube (:app/tubes grid-props)]
             (grid-row tube))))

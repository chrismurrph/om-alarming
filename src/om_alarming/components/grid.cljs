(ns om-alarming.components.grid
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names]]
            [om-alarming.business :as bus]))

(defui CheckBox
  Object
  (render [this]
    (let [props (om/props this)
          ;computed (om/get-computed this)
          test-props (:test-props props)
          selected (or (:selected props) (:selected test-props))
          ;full-name (or (:full-name computed) (:full-name test-props))
          ;_ (println full-name "is selected:" selected)
          ]
      (dom/div #js {:className (str "ui" (if selected " checked " " ") "checkbox")}
               (dom/input #js {:type "checkbox" :checked (when selected " ")})
               (dom/label nil "")))))

(def checkbox (om/factory CheckBox {:keyfn :id}))

;;
;; <div class="ui checkbox">
;; <input type="checkbox" name="example">
;; <label>Make my profile visible</label>
;; </div>
;;
(defui GridDataCell
  Object
  (render [this]
    (let [{:keys [id gas selected] :as props} (om/props this)
          gas-name (-> bus/gas->details gas :name)
          {:keys [tube-num]} (om/get-computed this)
          full-name (str "Tube " tube-num " " gas-name)
          ;_ (println full-name ", selected" selected)
          ]
      (dom/div #js {:className "three wide column center aligned"}
               (if (= gas :tube)
                 (dom/label nil tube-num)
                 (checkbox (om/computed props {:full-name full-name})))))))

(def grid-data-cell (om/factory GridDataCell {:keyfn :id}))

(defui GridRow
  Object
  (render [this]
    (let [{:keys [id gases]} (om/props this)
          hdr-gases (into [{:id 0 :gas :tube}] gases)]
      (dom/div #js {:className "row"}
               (for [gas hdr-gases]
                 (grid-data-cell (om/computed gas {:tube-num id})))))))

(defui GridHeaderRow
  Object
  (render [this]
    (let [{:keys [id gases]} (om/props this)]
      (dom/div #js {:className "row"}
               (for [gas gases
                     :let [gas-kw (:gas gas)
                           ;_ (println "GAS:" gas-kw)
                           gas-name (-> bus/gas->details gas-kw :name)
                           ;_ (println "gas:" gas-name)
                           ]]
                 (dom/div #js {:className "three wide column center aligned"}
                          (dom/label nil gas-name)))))))

(def grid-header-row (om/factory GridHeaderRow {:keyfn :id}))
(def grid-row (om/factory GridRow {:keyfn :id}))

(defui GasSelectionGrid
  Object
  (render [this]
    (let [tubes (:tubes (om/props this))
          gases (:gases (nth tubes 0))
          hdr-gases (into [{:id 0 :gas :tube}] gases)]
      (dom/div #js {:className "ui five column grid"}
               (grid-header-row {:gases hdr-gases})
               (for [tube tubes]
                 (grid-row tube))))))

(def gas-selection-grid (om/factory GasSelectionGrid {:keyfn :id}))
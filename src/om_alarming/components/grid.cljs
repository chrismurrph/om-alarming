(ns om-alarming.components.grid
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names]]
            [om-alarming.business :as bus]))

(defui CheckBox
  Object
  (render [this]
    (let [props (om/props this)
          computed (om/get-computed this)
          test-props (:test-props props)
          selected (or (:selected props) (:selected test-props))
          full-name (or (:full-name computed) (:full-name test-props))
          _ (println full-name "is selected:" selected)
          ]
      (dom/div #js {:className
                    (str "ui" (if selected " checked " " ") "checkbox")
                    ;"ui checked checkbox"
                    }
               (dom/input #js {:type "checkbox" :checked (when selected " ")})
               (dom/label nil "")))))

(def checkbox (om/factory CheckBox {:keyfn :id}))

;;
;; <div class="ui checkbox">
;; <input type="checkbox" name="example">
;; <label>Make my profile visible</label>
;; </div>
;;
(defui GridCell
  Object
  (render [this]
    (let [{:keys [id gas selected] :as props} (om/props this)
          gas-name (-> bus/gas->details gas :name)
          {:keys [tube-num]} (om/get-computed this)
          full-name (str "Tube " tube-num " " gas-name)
          _ (println full-name ", selected" selected)
          ]
      (dom/div #js {:className "two wide column center aligned"}
               (checkbox (om/computed props {:full-name full-name}))))))

(def grid-cell (om/factory GridCell {:keyfn :id}))

(defui GridRow
  Object
  (render [this]
    (let [{:keys [id gases]} (om/props this)]
      (dom/div #js {:className "row"}
               (for [gas gases]
                 (grid-cell (om/computed gas {:tube-num id})))))))

(def grid-row (om/factory GridRow {:keyfn :id}))

(defui GasSelectionGrid
  Object
  (render [this]
    (let [tubes (:tubes (om/props this))]
      (dom/div #js {:className "ui five column grid"}
               (for [tube tubes]
                 (grid-row tube))))))

(def gas-selection-grid (om/factory GasSelectionGrid {:keyfn :id}))
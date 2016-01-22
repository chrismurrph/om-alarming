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
    (let [{:keys [id gas] :as props} (om/props this)
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

(defui GridHeaderLabel
  Object
  (render [this]
    (let [{:keys [gas]} (om/props this)
          ;_ (println "GAS:" gas)
          gas-name (-> bus/gas->details gas :name)]
      (dom/div #js {:className "three wide column center aligned"}
               (dom/label nil gas-name)
               ))))

(def grid-header-label (om/factory GridHeaderLabel {:keyfn :id}))

;;
;; The props have been significantly altered by the time arrive here. Is this okay?
;; Probably not, hence now assiduously using computed props. To the strange extent
;; that GridHeaderLabel doesn't need any real props at all.
;; Hmm - I'm slightly bastardising props. I expect it won't matter for these low down
;; ones that won't even have queries associated with them
;;
(defui GridHeaderRow
  Object
  (render [this]
    (let [{:keys [gases]} (om/props this)
          hdr-gases (into [{:id 0 :gas :tube}] gases)]
      (dom/div #js {:className "row"}
               (for [gas hdr-gases]
                 (grid-header-label gas))))))

(def grid-header-row (om/factory GridHeaderRow {:keyfn :id}))
(def grid-row (om/factory GridRow {:keyfn :id}))

(defui GasSelectionGrid
  Object
  (render [this]
    (let [props (om/props this)
          tubes (:tubes props)
          gases (:gases (nth tubes 0))
          ;_ (println "Gases from first tube: " gases)
          ]
      (dom/div #js {:className "ui five column grid"}
               (grid-header-row {:gases gases})
               (for [tube tubes]
                 (grid-row tube))))))

(def gas-selection-grid (om/factory GasSelectionGrid {:keyfn :id}))
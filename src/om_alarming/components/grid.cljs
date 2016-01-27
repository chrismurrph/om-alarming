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
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:id props)])
  static om/IQuery
  (query [this]
    '[:id :gas :selected])
  Object
  (render [this]
    (let [{:keys [id gas] :as props} (om/props this)
          _ (assert gas (str "GridDataCell needs to be given a gas keyword, props: " props))
          gas-name (-> bus/gas->details gas :name)
          {:keys [tube-num]} (om/get-computed this)
          full-name (str "Tube " tube-num " " gas-name)
          _ (println full-name)
          ]
      (dom/div #js {:className "three wide column center aligned"}
               (if (= gas :tube)
                 (dom/label nil tube-num)
                 (checkbox (om/computed props {:full-name full-name})))))))

(def grid-data-cell (om/factory GridDataCell {:keyfn :id}))

(defui GridRow
  static om/Ident
  (ident [this props]
    [:tube/by-id (:id props)])
  static om/IQuery
  (query [this]
    `[:id {:tube/gases ~(om/get-query GridDataCell)}])
  Object
  (render [this]
    (let [{:keys [id tube/gases]} (om/props this)
          _ (println "gases: " gases)
          hdr-gases (into [{:id 0 :gas :tube}] gases)]
      (dom/div #js {:className "row"}
               (for [gas gases]
                 (grid-data-cell (om/computed gas {:tube-num id})))))))

(def grid-row (om/factory GridRow {:keyfn :id}))

(defui GridHeaderLabel
  Object
  (render [this]
    (let [{:keys [gas]} (om/props this)
          ;_ (println "GAS:" gas)
          gas-name (-> bus/gas->details gas :name)]
      (dom/div #js {:className "three wide column center aligned"}
               (dom/label nil gas-name)))))

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

;;
;; Make this just a function and the queries should compose from top to bottom
;;
;(defui GasSelectionGrid
;  static om/IQuery
;  (query [this]
;    [{:app/tubes (om/get-query GridRow)}
;     ])
;  Object
;  (render [this]
;    (let [props (om/props this)
;          {:keys [app/gases app/tubes]} props
;          ;_ (println "Grid has " (count gases) " gases and " (count tubes) " tubes")
;          ]
;      (dom/div #js {:className "ui five column grid"}
;               (grid-header-row {:gases gases})
;               (for [tube tubes]
;                 (grid-row tube))
;               ))))
;
;(def gas-selection-grid (om/factory GasSelectionGrid {:keyfn :id}))

(defn gas-selection-grid [grid-props]
  (dom/div #js {:className "ui five column grid"}
           (grid-header-row {:app/gases grid-props})
           (for [tube (:app/tubes grid-props)]
             (grid-row tube))))

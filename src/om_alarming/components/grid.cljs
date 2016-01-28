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
;; Need an Ident for db->query to work. These are just the gases themselves, so there might only be 4 of them
;;
(defui SystemGas
  static om/Ident
  (ident [this props]
    [:gas-of-system/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name]))

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
    [:id :selected {:system-gas (om/get-query SystemGas)}])
  Object
  (render [this]
    (let [{:keys [id system-gas] :as props} (om/props this)
          _ (assert system-gas (str "GridDataCell needs to be given a gas keyword, props: " props))
          ;gas-name (-> bus/gas->details system-gas :name)
          gas-name (:name system-gas)
          {:keys [tube-num]} (om/get-computed this)
          full-name (str "Tube " tube-num " " gas-name)
          ;_ (println full-name)
          ]
      (dom/div #js {:className "three wide column center aligned"}
               (if (= system-gas :tube)
                 (dom/label nil tube-num)
                 (checkbox (om/computed props {:full-name full-name})))))))

(def grid-data-cell (om/factory GridDataCell {:keyfn :id}))

(defui GridRow
  static om/Ident
  (ident [this props]
    [:tube/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id {:tube/gases (om/get-query GridDataCell)}])
  Object
  (render [this]
    (let [{:keys [id tube/gases]} (om/props this)
          ;_ (println "gases: " gases)
          hdr-gases (into [{:id 0 :gas :tube}] gases)]
      (dom/div #js {:className "row"}
               (for [gas gases]
                 (grid-data-cell (om/computed gas {:tube-num id})))))))

(def grid-row (om/factory GridRow {:keyfn :id}))

(defui GridHeaderLabel
  Object
  (render [this]
    (let [props (om/props this)
          ;_ (println "props:" props)
          {:keys [name]} props
          ;_ (println "GAS:" name)
          ;gas-name (-> bus/gas->details gas :name)
          ;gas-name (:name name)
          ]
      (dom/div #js {:className "three wide column center aligned"}
               (dom/label nil name)))))

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
    (let [{:keys [app/gases]} (om/props this)
          hdr-gases (into [{:id 0 :gas :tube}] gases)]
      (dom/div #js {:className "row"}
               (for [gas gases]
                 (grid-header-label gas))))))

(def grid-header-row (om/factory GridHeaderRow {:keyfn :id}))

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
;(def gas-selection-grid (om/factory GasSelectionGrid {:keyfn :id}))

(defn gas-selection-grid [grid-props]
  (dom/div #js {:className "ui five column grid"}
           (grid-header-row {:app/gases (:app/gases grid-props)})
           (for [tube (:app/tubes grid-props)]
             (grid-row tube))))

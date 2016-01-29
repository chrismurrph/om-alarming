(ns om-alarming.components.nav
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.parsing.mutates]
            [om-alarming.reconciler :refer [my-reconciler]]
            [om-alarming.util :refer [class-names]]))

(defui TabButton
  static om/Ident
  (ident [this props]
    [:button/by-id (:id props)])
  static om/IQuery
  (query [this]
    '[:id :name :description])
  Object
  (render [this]
    (let [{:keys [id name]} (om/props this)
          {:keys [selected]} (om/get-computed this)]
      (dom/a #js {:key id
                  :className (class-names {:item true :active selected})
                  :onClick #(om/transact! this `[(app/tab {:new-id ~id}) :app/selected-button])}
             name))))

(def tab-button (om/factory TabButton {:keyfn :id}))

(defui MenuBar
  Object
  (render [this]
    (let [{:keys [:app/buttons :app/selected-button]} (om/props this)
          _ (println "items: " buttons)
          _ (println "selected: " selected-button)
          selected-id (get selected-button :id)
          selected (first (filter #(= (:id %) selected-id) buttons))
          _ (println "heading: " (:description selected))
          ]
      (dom/div nil
               (dom/h3 #js {:className "ui block center aligned top attached header"} (:description selected))
               (dom/div #js {:className "ui tabular attached menu"}
                        (for [item buttons]
                          (when (not (= false (:showing item)))
                            (tab-button (om/computed item {:selected (= selected-id (:id item))})))))
               ;(dom/h3 #js {:className "ui left aligned attached header"} (str current-heading " has not been implemented"))
               ;(dom/div #js {:height 700 :className "ui attached segment"}
               ;         (dom/p nil "Content"))
               ;(dom/h5 #js {:className "ui bottom attached header"} (str current-heading " really has not been implemented"))
               ))))

(def menubar (om/factory MenuBar {:keyfn :id}))
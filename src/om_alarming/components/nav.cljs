(ns om-alarming.components.nav
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.parsing.mutates]
            [om-alarming.util :refer [class-names]]))

(defui TabButton
  static om/Ident
  (ident [this props]
    [:button/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :selected :name :description])
  Object
  (render [this]
    (let [{:keys [id selected name]} (om/props this)]
      (dom/a #js {:key id
                  :className (class-names {:item true :active selected})
                  :onClick #(om/transact! this `[(app/tab {:new-id ~id})])}
             name))))

(def tab-button (om/factory TabButton {:keyfn :id}))

(defn current-button [items]
  (first (filter :selected items)))

(defui MenuBar
  ;static om/IQuery
  ;(query [this]
  ;  [{:app/buttons (om/get-query TabButton)}
  ;   ])
  Object
  (render [this]
    (let [items (:app/buttons (om/props this))
          _ (println "items: " items)
          current-button-fn (partial current-button items)
          _ (println "heading: " (:description (current-button-fn)))
          ]
      (dom/div nil
               (dom/h3 #js {:className "ui block center aligned top attached header"} (:description (current-button-fn)))
               (dom/div #js {:className "ui tabular attached menu"}
                        (for [item items]
                          (when (not (= false (:showing item)))
                            (tab-button item))))
               ;(dom/h3 #js {:className "ui left aligned attached header"} (str current-heading " has not been implemented"))
               (dom/div #js {:className "ui attached segment"}
                        (dom/p #js {:height 300} "Content"))
               ;(dom/h5 #js {:className "ui bottom attached header"} (str current-heading " really has not been implemented"))
               ))))

(def menubar (om/factory MenuBar {:keyfn :id}))
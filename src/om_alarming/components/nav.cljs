(ns om-alarming.components.nav
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names index-of]]
            ))

(defui Button
  Object
  (render [this]
    (let [{:keys [id selected name]} (om/props this)]
      (dom/a #js {:key id :className (class-names {:item true :active selected})} name))))

(def button (om/factory Button {:keyfn :id}))

(defui MenuBar
  Object
  (render [this]
    (let [items (:buttons (om/props this))
          _ (println "Got " (count items))]
      (dom/div #js {:className "ui pointing menu"}
              (for [item items]
                (button item))))))

(def menubar (om/factory MenuBar {:keyfn :id}))
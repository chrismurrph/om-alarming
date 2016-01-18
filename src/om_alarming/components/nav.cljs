(ns om-alarming.components.nav
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names]]))

(defui TabButton
  Object
  (render [this]
    (let [{:keys [id selected name]} (om/props this)]
      (dom/a #js {:key id :className (class-names {:item true :active selected})} name))))

(def tab-button (om/factory TabButton {:keyfn :id}))

(defn current-heading [items]
  (:description (first (filter :selected items))))

(defui MenuBar
  Object
  (render [this]
    (let [items (:buttons (om/props this))
          current-heading (current-heading items)]
      (dom/div nil
               (dom/h3 #js {:className "ui block center aligned header"} current-heading)
               (dom/div #js {:className "ui tabular menu"}
                        (for [item items]
                          (when (not (= false (:showing item)))
                            (tab-button item))))
               (dom/h3 #js {:className "ui left aligned header"} (str current-heading " has not been implemented"))))))

(def menubar (om/factory MenuBar {:keyfn :id}))
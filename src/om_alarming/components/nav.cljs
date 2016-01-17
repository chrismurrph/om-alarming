(ns om-alarming.components.nav
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names index-of]]
            [om-bootstrap.nav :as n]))

(defn button [info]
  (n/nav-item {:key (:id info)} (:name info)))

(defui MenuBar
  Object
  (render [this]
    (let [items (:buttons (om/props this))
          ;_ (println "Got " (count items))
          ]
      (n/navbar {:brand (dom/a {:href "#"} "Navbar")}
        (n/nav {:bs-style "pills"
                :active-key 1
                :on-select (fn [k _] (js/alert (str "Selected " k)))}
               (for [item items]
                 (button item)))))))

(def menubar (om/factory MenuBar))
(ns om-alarming.components.nav
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names index-of]]))

(def button
  (dom/li "Hi Mum"))

(defui NavBar
  Object
  (render [this]
    (let [items (:buttons (om/props this))
          _ (println "Got " (count items))]
      (dom/ul nil
              (for [item items]
                button)))))

(def navbar (om/factory NavBar))
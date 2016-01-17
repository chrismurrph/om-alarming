(ns om-alarming.components.nav
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names index-of]]
            [cljsjs.react-bootstrap]
            ))

(defn button [info]
  ;(js/ReactBootstrap.Nav (js->clj {:key (:id info)}) (:name info))
  (dom/li (js->clj {:key (:id info)}) (:name info))
  )

(defui MenuBar
  Object
  (render [this]
    (let [items (:buttons (om/props this))
          ;_ (println "Got " (count items))
          ]

      (js/ReactBootstrap.Navbar (js->clj {:brand (dom/a {:href "#"} "Navbar")}))

      (js/ReactBootstrap.NavItem (js->clj {:bs-style   "pills"
                        :active-key 1
                        :on-select  (fn [k _] (js/alert (str "Selected " k)))})
              (for [item items]
                (button item)))

      ;(button {:id "A" :name "B"})

      )))

(def menubar (om/factory MenuBar))
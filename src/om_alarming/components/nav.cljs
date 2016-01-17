(ns om-alarming.components.nav
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names index-of]]
            ;[om-alarming.bootstrap :refer [navbar nav nav-item]]
            ;[sablono.core :as html :refer-macros [html]]
            ;[bootstrap-cljs :as bs :include-macros true]
            ))

(defn button [info]
  (js/ReactBootstrap.Nav (js->clj {:key (:id info)}) (:name info)))

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
                (button item))))))

(def menubar (om/factory MenuBar))
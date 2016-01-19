(ns om-alarming.graph.processing
  (:require [goog.string :as gstring]
            [goog.string.format]
            [om-alarming.utils :as u]
            [om-alarming.graph.known-data-model :refer [gray black white very-light-blue]]))

;;
;; Anything that originally comes from graphing that does have to do with Reagent or the state
;;

(defn- rgb-map-to-str [{r :r g :g b :b}]
  (str "rgb(" r "," g "," b ")"))

(defn- format-as-str [dec-pl val units]
  (gstring/format (str "%." dec-pl "f" units) val))

(defn- now-time [] (js/Date.))
(defn- seconds [js-date] (/ (.getTime js-date) 1000))

(def line-defaults
  {:stroke (rgb-map-to-str black)
   :stroke-width 1})

(def point-defaults
  {;:stroke (rgb-map-to-str black)
   ;:stroke-width 2
   :r 2})

;;
;; current-label used to be (:current-label @state)
;;
(defn- hidden? [line-id current-label]
  (let [current (:name current-label)]
    (not= current line-id)))
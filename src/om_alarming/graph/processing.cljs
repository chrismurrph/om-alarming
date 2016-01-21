(ns om-alarming.graph.processing
  (:require [goog.string :as gstring]
            [goog.string.format]
            [om-alarming.utils :as u]
            [om-alarming.graph.mock-values :refer [black]]))

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
   :strokeWidth 1})

(def point-defaults
  {;:stroke (rgb-map-to-str black)
   ;:stroke-width 2
   :r 2})

(defn find-line [my-lines name]
  (get my-lines name))

;;
;; current-label used to be (:current-label @state)
;;
(defn- hidden? [line-id current-label]
  (assert line-id)
  (let [current (:name current-label)
        _ (assert current)
        res (not= current line-id)
        ;_ (when res (println "Hidden b/c not equal: " current line-id))
        ]
    res))

(def default-init-state {:translator nil})
(def init-state (atom default-init-state))

(defn- staging-translators [min-x min-y max-x max-y graph-width graph-height]
  (let [horiz-trans-fn (fn [val] (u/scale {:min min-x :max max-x} {:min 0 :max graph-width} val))
        vert-trans-fn (fn [val] (u/scale {:min min-y :max max-y} {:min 0 :max graph-height} val))
        trans-point-fn (fn [[x [y val]]] [(horiz-trans-fn x) (vert-trans-fn y) val])
        ]
    {:horiz horiz-trans-fn :vert vert-trans-fn :point trans-point-fn}))

;;
;; keyword options:
;; :height :width :trans-point :get-positions :get-colour
;; All are defaulted - see main-component
;; Note that :trans-colour does not exist - colours have to be of shape {:r :g :b}
;;
(defn init [options-map]
  (let [staging (:staging options-map)
        graph-width (:width options-map)
        _ (assert graph-width ":width needs to be supplied at init")
        graph-height (:height options-map)
        _ (assert graph-height ":height needs to be supplied at init")
        translators (staging-translators (or (:min-x staging) 0) (or (:min-y staging) 0) (or (:max-x staging) 999)
                                         (or (:max-y staging) 999) graph-width graph-height)
        ]
    (swap! init-state assoc-in [:translator] translators)))
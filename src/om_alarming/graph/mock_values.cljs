(ns om-alarming.graph.mock-values
  (:require [om-alarming.util.utils :as u]))

(def black {:r 0 :g 0 :b 0})
(def white {:r 255 :g 255 :b 255})
(def blue {:r 0 :g 51 :b 102})
(def light-blue {:r 0 :g 204 :b 255})
(def very-light-blue {:r 235 :g 244 :b 245})
(def pink {:r 255 :g 0 :b 255})
(def brown {:r 102 :g 51 :b 0})
(def green {:r 0 :g 102 :b 0})
(def red {:r 255 :g 0 :b 0})
(def gray {:r 64 :g 64 :b 64})

(def graph-width 640)
(def graph-height 250)

;;
;; Client will know the width and height of the area it needs to put dots onto. These will change and every time
;; they do a new scale function can be requested here. Note that the world of this namespace is 0 - 999 inclusive.
;; If height changes a new height partial function will be requested.
;; At any time for drawing a dot on the actual canvas there will be two partial functions originally gained from
;; here. `scale-height` for the y value and `scale-width` for the x value.
;;
(defn scale-fn [width-or-height]
  (partial u/scale {:min 0 :max 999} {:min 0 :max width-or-height}))

(defn remover [idx coll]
  (u/vec-remove coll idx))

;;
;; Using actual values here, which staging area will translate into y values.
;;
(def methanes [0.03 0.04 0.05 0.04 0.03 0.02 0.01 0.01 0.07 0.07 0.10 0.13])
(def oxygens [21.0 22.0 22.0 21.4 20.0 21.3 22.0 19.5 19.8 21.0 21.1 21.5])
(def carbon-dioxides [0.05 0.08 0.07 0.09 0.10 0.20 0.21 0.23 0.27 0.13 0.18 0.19])
(def carbon-monoxides [7 8 9 10 11 12 11 10 9 8 7 6 5])
(def gas-values {"Methane at 1" methanes "Oxygen at 4" oxygens "Carbon Dioxide at 2" carbon-dioxides "Carbon Monoxide at 3" carbon-monoxides})

(defn random-gas-value [name]
  (let [vec-of (get gas-values name)
        its-size (count vec-of)
        idx (rand-int its-size)
        val (nth vec-of idx)
        _ (assert val (str "No random gas value found for: " name))
        ;_ (u/log "name: " name ", value: " val)
        ]
    val))

;;
;; All the lines that get graphed. Has nothing to do with Reagent, so use a normal atom.
;; :x and :y are both always in the range 0-999. Scaling to the actual graph enclosure is done at drawing time
;; i.e. completely dynamically.
;; Note usual computer/graphical rules apply, so x starts at 0 and goes across the top, and y then goes down from
;; the top.
;; Note that the x value is always increasing, meaning older come before newer, because x is time.
;;
(def lines (atom [
                  {:colour pink
                   :name "Methane at 1"
                   :units "%"}
                  {:colour green
                   :name "Oxygen at 4"
                   :units "%"}
                  {:colour blue
                   :name "Carbon Dioxide at 2"
                   :units "%"}
                  {:colour red
                   :name "Carbon Monoxide at 3"
                   :units "ppm"}
                  ]))

(def my-lines {"Methane at 1" {:name "Methane" :units "%" :colour pink :points methanes}
               "Oxygen at 4" {:name "Oxygen at 4" :units "%" :colour green :points oxygens}
               "Carbon Dioxide at 2" {:name "Carbon Dioxide at 2" :units "%" :colour blue :points carbon-dioxides}
               "Carbon Monoxide at 3" {:name "Carbon Monoxide at 3" :units "ppm" :colour red :points carbon-monoxides}})

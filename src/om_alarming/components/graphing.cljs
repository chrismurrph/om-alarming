(ns om-alarming.components.graphing
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names]]
            [om-alarming.graph.processing :as process]))

;;
;; (defn- text-component [x y-intersect colour-str txt-with-units line-id]
;; [:text {:opacity (if (hidden? line-id) 0.0 1.0) :x (+ x 10) :y (+ (:proportional-y y-intersect) 4) :font-size "0.8em" :stroke colour-str}
;; (format-as-str (or (:dec-places y-intersect) 2) (:proportional-val y-intersect) txt-with-units)])
;;
(defui TextComponent
  Object
  (render [this]
    (let [{:keys [x y-intersect colour-str txt-with-units line-id current-label]} (om/props this)
          _ (println "TXT:" txt-with-units)]
      (dom/text #js {:opacity (if (process/hidden? line-id current-label) 0.0 1.0)
                     :x (+ x 10)
                     :y (+ (:proportional-y y-intersect) 4)
                     :fontSize "0.8em"
                     :stroke colour-str}
                (process/format-as-str (or (:dec-places y-intersect) 2) (:proportional-val y-intersect) txt-with-units)))))

(def text-component (om/factory TextComponent {:keyfn :id}))

(defui SimpleSVG
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [width height]} props
          test-props (:test-props props)]
      (dom/div nil
               (dom/svg {:width width :height height})
               (text-component test-props)))))

(def simple-svg (om/factory SimpleSVG {:keyfn :id}))


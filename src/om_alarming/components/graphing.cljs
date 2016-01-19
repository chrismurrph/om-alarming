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
          text-props {:opacity (if (process/hidden? line-id current-label) 0.0 1.0)
                      :x (+ x 10)
                      :y (+ (:proportional-y y-intersect) 4)
                      :fontSize "0.8em"
                      :stroke colour-str}
          _ (println text-props)]
      (dom/text (clj->js text-props)
                (process/format-as-str (or (:dec-places y-intersect) 2) (:proportional-val y-intersect) txt-with-units)))))

(def text-component (om/factory TextComponent {:keyfn :id}))

;;
;;(defn- plum-line [height visible x-position]
;;  (let [currently-sticky (get-in @state [:in-sticky-time?])
;;        res (when visible [:line
;;                           (merge line-defaults
;;                                  {:x1 x-position :y1 0
;;                                   :x2 x-position :y2 height
;;                                   :stroke-width (if currently-sticky 2 1)})
;; ])]
;;    res))
;;
(defui PlumbLine
  Object
  (render [this]
    (let [{:keys [height visible? x-position in-sticky-time?]} (om/props this)
          stroke-width (if in-sticky-time? 2 1)
          line-props (merge process/line-defaults
                            {:x1           x-position
                             :y1           0
                             :x2           x-position
                             :y2           height
                             :strokeWidth stroke-width})
          _ (println line-props)
          res (when visible? (dom/line (clj->js line-props)))]
      res)))

(def plumb-line (om/factory PlumbLine {:keyfn :id}))

(defn testing-component [name test-props]
  (case name
    "text-component" (text-component test-props)
    "plumb-line" (plumb-line test-props)))

(defui SimpleSVGTester
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [width height]} props
          test-props (:test-props props)
          testing-name (:testing-name test-props)]
      (dom/div nil
               (dom/svg {:width width :height height}
                        (testing-component testing-name test-props))))))

(def simple-svg-tester (om/factory SimpleSVGTester {:keyfn :id}))


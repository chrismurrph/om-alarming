(ns om-alarming.components.graphing
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names]]
            [om-alarming.graph.processing :as process]
            [om-alarming.graph.known-data-model :refer [white black]]))

;;;;
;;;; Using another :g means this is on a different layer so the text that is put on top of this rect does not have its
;;;; opacity affected.
;;;;
;;(defn- opaque-rect [x y line-id]
;;  (let [height 16
;;        half-height (/ height 2)
;;        width 45 ;; later we might use how many digits there are
;;        indent 8
;;        width-after-indent (- width 4)]
;;    [:g [:rect {:x (+ indent x) :y (- y half-height) :width width-after-indent :height height :opacity (if (hidden? line-id) 0.0 1.0) :fill (rgb-map-to-str white) :rx 5 :ry 5}]]))
;;

(defui OpaqueRect
  Object
  (render [this]
    (let [{:keys [x y line-id current-label]} (om/props this)
          height 16
          half-height (/ height 2)
          width 45 ;; later we might use how many digits there are
          indent 8
          width-after-indent (- width 4)
          new-x (+ indent x)
          new-y (- y half-height)
          opacity (if (process/hidden? line-id current-label) 0.0 1.0)
          fill (process/rgb-map-to-str black)
          rect-props {:x new-x :y new-y :width width-after-indent :height height :opacity opacity :fill fill :rx 5 :ry 5}
          _ (println "rect-props: " rect-props)
          ]
      (dom/g nil (dom/rect (clj->js rect-props))))))

(def opaque-rect (om/factory OpaqueRect {:keyfn :id}))

;;
;; (defn- text-component [x y-intersect colour-str txt-with-units line-id]
;; [:text {:opacity (if (hidden? line-id) 0.0 1.0) :x (+ x 10) :y (+ (:proportional-y y-intersect) 4) :font-size "0.8em" :stroke colour-str}
;; (format-as-str (or (:dec-places y-intersect) 2) (:proportional-val y-intersect) txt-with-units)])
;;
(defui TextComponent
  Object
  (render [this]
    (let [{:keys [x y-intersect colour-str txt-with-units line-id current-label testing-name]} (om/props this)
          lower-by (if testing-name 50 0)
          text-props {:opacity  (if (process/hidden? line-id current-label) 0.0 1.0)
                      :x        (+ x 10)
                      :y        (+ lower-by (+ (:proportional-y y-intersect) 4))
                      :fontSize "0.8em"
                      :stroke   colour-str}
          ;_ (println text-props)
          ]
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
          res (when visible? (dom/line (clj->js line-props)))]
      res)))

(def plumb-line (om/factory PlumbLine {:keyfn :id}))

(defn testing-component [name test-props]
  (case name
    "opaque-rect" (opaque-rect test-props)
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


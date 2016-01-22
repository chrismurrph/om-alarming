(ns om-alarming.components.graphing
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.components.surplus :as surplus]
            [om-alarming.graph.processing :as process]
            [om-alarming.graph.mock-values :refer [white light-blue black]]))

;;
;;(defn- point [rgb-map x y]
;;  ;(log rgb-map)
;;  [:circle
;;   (merge point-defaults
;;          {:cx x
;;           :cy y
;;           :fill (rgb-map-to-str rgb-map)})])
;;
(defui Point
  Object
  (render [this]
    (let [{:keys [rgb-map x y]} (om/get-computed this)
          _ (println "POINT: " rgb-map " " x " " y)
          circle-props    (merge process/point-defaults
                                 {:cx x
                                  :cy y
                                  :fill (process/rgb-map-to-str rgb-map)})]
      (dom/circle (clj->js circle-props)))))

(def point (om/factory Point {:keyfn :id}))

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

(defui RectTextTick
  Object
  (render [this]
    (let [{:keys [x-gas-info current-label x id my-lines]} (om/props this)
          {:keys [proportional-y proportional-val name testing-name]} x-gas-info
          _ (assert name (str "x-gas-info w/out a name: " x-gas-info))
          _ (println "my-lines count: " (count my-lines))
          ;;; text
          line-doing (process/find-line my-lines name)
          _ (assert line-doing (str "Not found a name for <" name "> from:" my-lines))
          text-colour-str (-> line-doing :colour process/rgb-map-to-str)
          ;_ (println "colour will be " colour-str)
          units-str (:units line-doing)
          line-id (:name line-doing)
          text-props {:opacity  (if (process/hidden? line-id current-label) 0.0 1.0)
                      :x        (+ x 10)
                      :y        (+ proportional-y 4)
                      :fontSize "0.8em"
                      :stroke   text-colour-str}
          ;_ (println text-props)
          ;;; tick
          colour-str (-> line-doing :colour process/rgb-map-to-str)
          ;_ (println (:name drop-info) " going to be " colour-str)
          drop-distance proportional-y
          _ (assert drop-distance)
          line-props (merge process/line-defaults
                            {:id      drop-distance
                             :x1      x :y1 drop-distance
                             :x2      (+ x 6) :y2 drop-distance
                             :stroke  colour-str
                             :opacity (if (process/hidden? (:name line-doing) current-label) 0.0 1.0)})
          ;;; rect
          height 16
          half-height (/ height 2)
          width 45 ;; later we might use how many digits there are
          indent 8
          width-after-indent (- width 4)
          new-x (+ indent x)
          new-y (- proportional-y half-height)
          opacity (if (process/hidden? name current-label) 0.0 1.0)
          _ (when (= 0 opacity) (println "name:" name ", current-label" current-label "not being displayed"))
          colour (if testing-name light-blue white)
          fill (process/rgb-map-to-str colour)
          rect-props {:x new-x :y new-y :width width-after-indent :height height :opacity opacity :fill fill :rx 5 :ry 5}
          _ (println "rect-props: " rect-props)
          ]
      (dom/g nil
             (dom/rect (clj->js rect-props))
             (dom/text (clj->js text-props)(process/format-as-str (or (:dec-places current-label) 2) proportional-val units-str))
             (dom/line (clj->js line-props))))))

(def rect-text-tick (om/factory RectTextTick {:keyfn :id}))

(defn many-rect-text-ticks [drop-info]
  (let [{:keys [x-gas-details current-label x my-lines]} drop-info]
    (println "count x-gas-details: " (count x-gas-details))
    (println "names: " (map :name x-gas-details))
    (assert (:name current-label))
    (assert my-lines)
    (for [x-gas-info x-gas-details]
      (rect-text-tick {:x-gas-info x-gas-info
                       :current-label current-label
                       :x x
                       :my-lines my-lines
                       :id (:id x-gas-info)}))))

(defui ManyRectTextTick
  Object
  (render [this]
    (let [{:keys [drop-info testing-name]} (om/props this)]
      (if testing-name
        (dom/g nil
               (many-rect-text-ticks drop-info))
        (many-rect-text-ticks drop-info)))))
(def many-rect-text-tick (om/factory ManyRectTextTick {:keyfn :id}))

(defn testing-component [name test-props]
  (case name
    "plumb-line" (plumb-line test-props)
    "point" (point (om/computed {} test-props))
    "rect-text-tick" (rect-text-tick test-props)
    "many-rect-text-tick" (many-rect-text-tick test-props)
    ;; These may all be surplus:
    "tick-lines" (surplus/tick-lines test-props)
    "text-component" (surplus/text-component test-props)
    "backing-rects" (surplus/backing-rects test-props)
    "insert-texts" (surplus/insert-texts test-props)
    "opaque-rect" (surplus/opaque-rect test-props)
    ))

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


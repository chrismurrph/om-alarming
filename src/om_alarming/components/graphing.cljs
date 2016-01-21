(ns om-alarming.components.graphing
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util :refer [class-names]]
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
    (let [{:keys [x proportional-y name current-label testing-name]} (om/props this)
          height 16
          half-height (/ height 2)
          width 45 ;; later we might use how many digits there are
          indent 8
          width-after-indent (- width 4)
          new-x (+ indent x)
          new-y (- proportional-y half-height)
          opacity (if (process/hidden? name current-label) 0.0 1.0)
          colour (if testing-name light-blue white)
          fill (process/rgb-map-to-str colour)
          rect-props {:x new-x :y new-y :width width-after-indent :height height :opacity opacity :fill fill :rx 5 :ry 5}
          ;_ (println "rect-props: " rect-props)
          ]
      (dom/g nil (dom/rect (clj->js rect-props))))))

(def opaque-rect (om/factory OpaqueRect {:keyfn :id}))

;;
;;(defn- backing-rects [x drop-infos]
;;  (for [drop-info drop-infos
;;        :let [y (:proportional-y drop-info)
;;              line-id (:name drop-info)]]
;;    ^{:key y} [opaque-rect x y line-id])
;;  )
;;

(defn many-rects [drop-infos]
  (for [drop-info drop-infos]
    (opaque-rect drop-info)))
;;
;; Need some kind of container when stand alone
;;
(defui BackingRects
  Object
  (render [this]
    (let [{:keys [drop-infos testing-name]} (om/props this)
          _ (println "testing-name: " testing-name)
          ]
      (if testing-name
        (dom/g nil
               (many-rects drop-infos))
        (many-rects drop-infos)))))

(def backing-rects (om/factory BackingRects {:keyfn :id}))

;;
;; (defn- text-component [x y-intersect colour-str txt-with-units line-id]
;; [:text {:opacity (if (hidden? line-id) 0.0 1.0) :x (+ x 10) :y (+ (:proportional-y y-intersect) 4) :font-size "0.8em" :stroke colour-str}
;; (format-as-str (or (:dec-places y-intersect) 2) (:proportional-val y-intersect) txt-with-units)])
;;
(defui TextComponent
  Object
  (render [this]
    (let [{:keys [x proportional-y proportional-val dec-places name my-lines current-label]} (om/props this)
          line-doing (process/find-line my-lines name)
          _ (assert line-doing (str "Not found a name for " name " from " my-lines))
          colour-str (-> line-doing :colour process/rgb-map-to-str)
          ;_ (println "colour will be " colour-str)
          units-str (:units line-doing)
          line-id (:name line-doing)
          text-props {:opacity  (if (process/hidden? line-id current-label) 0.0 1.0)
                      :x        (+ x 10)
                      :y        (+ proportional-y 4)
                      :fontSize "0.8em"
                      :stroke   colour-str}
          ;_ (println text-props)
          ]
      (dom/text (clj->js text-props)
                (process/format-as-str (or dec-places 2) proportional-val units-str)))))

(def text-component (om/factory TextComponent {:keyfn :id}))

(defn many-texts [drop-infos]
  (for [drop-info drop-infos]
    (text-component drop-info)))

;;
;; Need some kind of container when stand alone
;;
(defui InsertTexts
  Object
  (render [this]
    (let [{:keys [drop-infos testing-name]} (om/props this)
          ;_ (println drop-infos)
          ]
      (if testing-name
        (dom/g nil
               (many-texts drop-infos))
        (many-texts drop-infos)))))

(def insert-texts (om/factory InsertTexts {:keyfn :id}))

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

(defn alter-react-id [new-word infos]
  (map (fn [info] (update-in info [:id] (fn [id] (str new-word id)))) infos))

(defui TickLines
  Object
  (render [this]
    (let [{:keys [visible? drop-infos]} (om/props this)]
      ;(println "visible: " visible?)
      (when visible?
        (dom/g nil
               (many-rects drop-infos)
               (many-texts drop-infos)
               (for [drop-info drop-infos
                     :let [_ (println "DROP: " drop-info)
                           my-lines (:my-lines drop-info)
                           find-line (partial process/find-line my-lines)
                           line-doing (-> drop-info :name find-line)
                           ;_ (println "line-doing is " line-doing)
                           colour-str (-> line-doing :colour process/rgb-map-to-str)
                           ;_ (println (:name drop-info) " going to be " colour-str)
                           drop-distance (:proportional-y drop-info)
                           _ (assert drop-distance)
                           x (:x drop-info)
                           current-label (:current-label drop-info)
                           line-props (merge process/line-defaults
                                             {:id      drop-distance
                                              :x1      x :y1 drop-distance
                                              :x2      (+ x 6) :y2 drop-distance
                                              :stroke  colour-str
                                              :opacity (if (process/hidden? (:name line-doing) current-label) 0.0 1.0)})
                           res (dom/line (clj->js line-props))]]
                 res
                 ))))))

(def tick-lines (om/factory TickLines {:keyfn :id}))

(defn testing-component [name test-props]
  (case name
    "opaque-rect" (opaque-rect test-props)
    "text-component" (text-component test-props)
    "plumb-line" (plumb-line test-props)
    "backing-rects" (backing-rects test-props)
    "insert-texts" (insert-texts test-props)
    "tick-lines" (tick-lines test-props)
    "point" (point (om/computed {} test-props))
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


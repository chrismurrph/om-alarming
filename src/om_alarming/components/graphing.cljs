(ns om-alarming.components.graphing
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout]]
            [om-alarming.graph.processing :as process]
            [om-alarming.util.utils :as u]
            [om-alarming.graph.mock-values :refer [white light-blue black]]
            [om-alarming.components.general :as gen]
            [cljs.pprint :as pp :refer [pprint]]
            ))

(def careless-text-props (clj->js {:x  10 :y 20
                          :stroke      (process/rgb-map-to-str black)
                          :strokeWidth 0.25
                          :opacity     1.0}))

(defui Point
  static om/Ident
  (ident [this props]
    [:graph-point/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :x :y :val])
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [id x y val]} props
          {:keys [rgb-map translator]} (om/get-computed this)
          ;_ (println "POINT: " id " " rgb-map " " x " " y " " translator)
          _ (assert id)
          _ (assert (and x y val))
          [x-trans y-trans val-trans] (translator props)
          circle-props    (merge process/point-defaults
                                 {:cx x-trans
                                  :cy y-trans
                                  :fill (process/rgb-map-to-str rgb-map)})]
      (dom/circle (clj->js circle-props)))))
(def point-component (om/factory Point {:keyfn :id}))

;;
;; GridDataCell should be taking care of this one
;;
(defui Intersect
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :value {:tube (om/get-query gen/Location)}
     {:system-gas (om/get-query gen/SystemGas)}]))

(defui Misc
  static om/Ident
  (ident [this props]
    [:misc/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :comms :receiving-chan]))

(defui Line
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name :units :colour {:intersect (om/get-query Intersect)}
     {:graph/points (om/get-query Point)}
     ])
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [point-fn]} (om/get-computed this)
          _ (assert point-fn)
          ;_ (println "Line props:" props)
          {:keys [name units colour intersect graph/points]} props
          ;_ (assert (pos? (count points)) (str "No points found in:" props))
          ;_ (println "POINTs count: " (count points))
          ]
      (dom/g nil (for [point points]
                   (point-component (om/computed point {:rgb-map colour :translator point-fn})))))))
(def line-component (om/factory Line {:keyfn :id}))

(defui RectTextTick
  static om/Ident
  (ident [this props]
    [:x-gas-detail/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id {:graph/line (om/get-query Line)}])
  Object
  (render [this]
    (let [{:keys [id graph/line]} (om/props this)
          {:keys [current-line x testing-name proportional-y proportional-val]} (om/get-computed this)]
      (when proportional-y
        (let [_ (assert id)
              _ (assert line (str "x-gas-info w/out a line. \nCOMPUTED:\n" (om/get-computed this)
                                  "\nPROPs:\n" (om/props this)))
              ;;; text ;;;
              _ (assert current-line (str "Not found a current-line"))
              _ (assert line (str "Which line is this for?"))
              text-colour-str (-> current-line :colour process/rgb-map-to-str)
              ;_ (println "colour will be " colour-str)
              units-str (:units current-line)
              hidden? (not= line current-line)
              ;_ (when (not hidden?) (println "= " (:name line) (:name current-line)))
              text-props {:opacity     (if hidden? 0.0 1.0)
                          :x           (+ x 10)
                          :y           (+ proportional-y 4)
                          :fontSize    "0.8em"
                          :stroke      text-colour-str
                          :strokeWidth 0.65}
              ;_ (println text-props)
              ;;; tick ;;;
              colour-str (-> current-line :colour process/rgb-map-to-str)
              ;_ (println (:name drop-info) " going to be " colour-str)
              ;_ (assert proportional-y)
              line-props (merge process/line-defaults
                                {:id      proportional-y
                                 :x1      x :y1 proportional-y
                                 :x2      (+ x 6) :y2 proportional-y
                                 :stroke  colour-str
                                 :opacity (if hidden? 0.0 1.0)})
              ;;; rect ;;;
              height 16
              half-height (/ height 2)
              width 45                                      ;; later we might use how many digits there are
              indent 8
              width-after-indent (- width 4)
              new-x (+ indent x)
              new-y (- proportional-y half-height)
              opacity (if hidden? 0.0 1.0)
              ;_ (when (= 0 opacity) (println "current-label :-" current-line "not being displayed"))
              colour (if testing-name light-blue white)
              fill (process/rgb-map-to-str colour)
              rect-props {:x new-x :y new-y :width width-after-indent :height height :opacity opacity :fill fill :rx 5 :ry 5}
              ;_ (println "rect-props: " rect-props)
              ]
          (dom/g nil
                 (dom/rect (clj->js rect-props))
                 (dom/text (clj->js text-props) (process/format-as-str (or (:dec-places current-line) 2) proportional-val units-str))
                 (dom/line (clj->js line-props))))))))
(def rect-text-tick (om/factory RectTextTick {:keyfn :id}))

(defn rect-text-ticks [drop-info]
  (let [{:keys [graph/x-gas-details x-position testing-name graph/current-line horiz-fn point-fn]} drop-info
        _ (assert (and point-fn horiz-fn))
        _ (println "x-position: " x-position)
        points (:graph/points current-line)
        ]
    (when (and x-position (not-empty points))
      (let [pair (process/enclosed-by horiz-fn points x-position)
            _ (println "pair: " pair)
            proportionals (when pair (u/bisect-vertical-between (point-fn (first pair)) (point-fn (second pair)) x-position))
            _ (println "proportionals: " proportionals)]
        (println "count x-gas-details: " (count x-gas-details))
        ;(assert (:name current-label) (str "current-label has no name: <" current-label ">"))
        (dom/g nil
               (for [x-gas-info x-gas-details]
                 (rect-text-tick (om/computed x-gas-info (merge {:current-line     current-line
                                                                 :x                x-position
                                                                 :testing-name     testing-name
                                                                 :proportional-val nil
                                                                 :proportional-y   nil} proportionals)))))))))

(defui PlumbLine
  static om/Ident
  (ident [this props]
    [:plumb-line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :height :visible? :x-position :in-sticky-time?
     {:graph/x-gas-details (om/get-query RectTextTick)}
     {:graph/current-line (om/get-query Line)}
     ])
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [id height visible? x-position in-sticky-time?]} props
          {:keys [horiz-fn point-fn]} (om/get-computed this)
          _ (assert horiz-fn)
          stroke-width (if in-sticky-time? 2 1)
          line-props (merge process/line-defaults
                            {:x1           x-position
                             :y1           0
                             :x2           x-position
                             :y2           height
                             :strokeWidth stroke-width})
          res (when visible?
                (dom/g nil
                       (rect-text-ticks (merge props {:horiz-fn horiz-fn :point-fn point-fn}))
                       (dom/line (clj->js line-props))))]
      res)))
(def plumb-line-component (om/factory PlumbLine {:keyfn :id}))

(defui Label
  static om/Ident
  (ident [this props]
    [:label/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name :dec-places]))

(defui TrendingGraph
  static om/Ident
  (ident [this props]
    [:trending-graph/by-id (:id props)])  
  static om/IQuery
  (query [this]
    [:id
     :width 
     :height
     :receiving?
     :last-mouse-moment
     {:graph/lines (om/get-query Line)}
     :hover-pos
     :labels-visible?
     {:graph/misc (om/get-query Misc)}
     {:graph/plumb-line (om/get-query PlumbLine)}
     ;{:graph/drop-info (om/get-query DropInfo)}
     {:graph/translators [:point-fn :horiz-fn]}])
  Object
  (handler-fn [this comms-channel e]
    (assert comms-channel)
    (let [bounds (. (dom/node this) getBoundingClientRect)
          y (- (.-clientY e) (.-top bounds))
          x (- (.-clientX e) (.-left bounds))
          ;_ (println x y "in" comms-channel)
          ]
      (put! comms-channel {:type (.-type e) :x x :y y})
      nil))
  (render [this]
    (let [props (om/props this)
          ;_ (pprint props)
          {:keys [width
                  height
                  graph/lines 
                  hover-pos 
                  graph/labels-visible? 
                  graph/misc graph/plumb-line graph/translators]} props
          _ (assert (and width height) (str "No width or height in: <" props ">"))
          {:keys [point-fn horiz-fn]} translators
          _ (assert point-fn)
          _ (assert horiz-fn)
          comms-channel (:comms misc)
          _ (assert comms-channel "Need a comms channel to direct mouse movement at")
          handler #(.handler-fn this comms-channel %)
          handlers {:onMouseMove handler :onMouseUp handler :onMouseDown handler}
          init {:width width :height height}
          init-props (merge {:style {:border "thin solid black"}} init handlers)
          ;_ (println "SVG: " init-props)
          ;_ (println "LINEs count: " (count lines))
          ;_ (println "for-drop-info" for-drop-info)
          ]
      (dom/div nil
               (dom/svg (clj->js init-props)
                        (for [line lines]
                          (line-component (om/computed line translators)))
                        (plumb-line-component (om/computed (merge plumb-line init) translators))
                        )
               (dom/div nil "Here goes timing information")))))
(def trending-graph (om/factory TrendingGraph {:keyfn :id}))

(defn testing-component [name test-props]
  (case name
    "plumb-line" (plumb-line-component test-props)
    "point" (point-component test-props)
    "line" (line-component test-props)
    "rect-text-tick" (rect-text-tick test-props)
    ;"many-rect-text-tick" (drop-info-component test-props)
    ))

(defui SimpleSVGTester
  Object
  (render [this]
    (let [props (om/props this)
          _ (println "PR:" props)
          {:keys [width height]} props
          test-props (:test-props props)
          testing-name (:testing-name test-props)]
      (dom/div nil
               (dom/svg {:width width :height height}
                        (testing-component testing-name test-props))))))

(def simple-svg-tester (om/factory SimpleSVGTester {:keyfn :id}))


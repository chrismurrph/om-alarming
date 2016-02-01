(ns om-alarming.components.graphing
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.components.surplus :as surplus]
            [om-alarming.graph.processing :as process]
            [om-alarming.graph.mock-values :refer [white light-blue black]]
            [om-alarming.components.general :as gen]
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
    [:id :x :y :rgb-map])
  Object
  (render [this]
    (let [{:keys [id rgb-map x y]} (om/props this)
          _ (println "POINT: " id " " rgb-map " " x " " y)
          circle-props    (merge process/point-defaults
                                 {:cx x
                                  :cy y
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
    [:id :value {:tube (om/get-query gen/Location)} {:system-gas (om/get-query gen/SystemGas)}]))

(defui Line
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name :units :colour {:intersect (om/get-query Intersect)} {:graph/points (om/get-query Point)}])
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [name units colour intersect graph/points]} props
          _ (assert (pos? (count points)) (str "No points found in:" props))]
      (println "POINTs count: " (count points))
      (for [point points]
        (point-component point)))))

(def line-component (om/factory Line {:keyfn :id}))

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
  static om/Ident
  (ident [this props]
    [:x-gas-details/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:proportional-y :proportional-val :name])
  Object
  (render [this]
    (let [{:keys [proportional-y proportional-val name]} (om/props this)
          {:keys [current-label x lines testing-name]} (om/get-computed this)
          _ (assert name (str "x-gas-info w/out a name. \nCOMPUTED:\n" (om/get-computed this) "\nPROPs:\n" (om/props this)))
          _ (assert (pos? (count lines)))
          ;;; text
          line-doing (process/find-line lines name)
          _ (assert line-doing (str "Not found a name for <" name "> from:" lines))
          text-colour-str (-> line-doing :colour process/rgb-map-to-str)
          ;_ (println "colour will be " colour-str)
          units-str (:units line-doing)
          line-id (:name line-doing)
          text-props {:opacity  (if (process/hidden? line-id current-label) 0.0 1.0)
                      :x        (+ x 10)
                      :y        (+ proportional-y 4)
                      :fontSize "0.8em"
                      :stroke   text-colour-str
                      :strokeWidth 0.65}
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
  (let [{:keys [x-gas-details current-label x lines testing-name]} drop-info]
    (println "count x-gas-details: " (count x-gas-details))
    (println "names: " (map :name x-gas-details))
    (assert (:name current-label))
    (assert lines, "Expect lines in drop-info")
    (for [x-gas-info x-gas-details]
      (rect-text-tick (om/computed x-gas-info {:current-label current-label
                                               :x x
                                               :lines lines
                                               :testing-name testing-name})))))

(defui Label
  static om/Ident
  (ident [this props]
    [:label/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name :dec-places]))

(defui ManyRectTextTick
  static om/Ident
  (ident [this props]
    [:drop-info/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :x
     {:graph/x-gas-details (om/get-query RectTextTick)}
     {:current-label (om/get-query Label)}
     {:graph/lines (om/get-query Line)}])
  Object
  (render [this]
    (let [{:keys [drop-info testing-name]} (om/props this)]
      (if testing-name
        (dom/g nil
               (many-rect-text-ticks drop-info))
        (many-rect-text-ticks drop-info)))))
(def many-rect-text-tick (om/factory ManyRectTextTick {:keyfn :id}))

(defui MainComponent
  static om/IQuery
  (query [this]
    [{:graph/init [:width :height]} :graph/lines :graph/hover-pos :graph/labels-visible?])
  Object
  (render [this]
    (let [{:keys [graph/init graph/lines graph/hover-pos graph/labels-visible?]} (om/props this)
          {:keys [height width]} init
          _ (assert (and width height) (str "No width or height in: " init))
          init-props (merge {:style {:border "thin solid black"}} init)
          _ (println "SVG: " init-props)]
          _ (println "LINEs count: " (count lines))
      (dom/div nil
               (dom/svg (clj->js init-props)
                        (dom/g nil
                               (for [line lines]
                                 (line-component line))))))))
(def main-component (om/factory MainComponent {:keyfn :id}))

(defn testing-component [name test-props]
  (case name
    "plumb-line" (plumb-line test-props)
    "point" (point-component (om/computed {} test-props))
    "rect-text-tick" (rect-text-tick test-props)
    "many-rect-text-tick" (many-rect-text-tick test-props)
    ;; These may all be surplus:
    "poly" (surplus/poly test-props)
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
          _ (println "PR:" props)
          {:keys [width height]} props
          test-props (:test-props props)
          testing-name (:testing-name test-props)]
      (dom/div nil
               (dom/svg {:width width :height height}
                        (testing-component testing-name test-props))))))

(def simple-svg-tester (om/factory SimpleSVGTester {:keyfn :id}))


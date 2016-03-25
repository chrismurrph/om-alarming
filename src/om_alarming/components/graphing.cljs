(ns om-alarming.components.graphing
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout]]
            [om-alarming.graph.processing :as process]
            [om-alarming.util.utils :as u]
            [om-alarming.util.colours :refer [white light-blue black]]
            [om-alarming.components.general :as gen]
            [om-alarming.components.navigator :as navigator]
            [cljs.pprint :as pp :refer [pprint]]
            [om-alarming.reconciler :as reconciler])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def careless-text-props (clj->js {:x  10 :y 20
                          :stroke      (process/rgb-map-to-str black)
                          :strokeWidth 0.25
                          :opacity     1.0}))

(defui Point
  ;static om/Ident
  ;(ident [this props]
  ;  [:graph-point/by-id (:point-id props)])
  ;static om/IQuery
  ;(query [this]
  ;  [:point-id :x :y :val])
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [point-id x y val]} props
          {:keys [rgb-map translator]} (om/get-computed this)
          _ (println "POINT: " rgb-map " " x " " y)
          _ (assert point-id)
          _ (assert (and x y val))
          [x-trans y-trans val-trans] (translator props)
          circle-props    (merge process/point-defaults
                                 {:cx x-trans
                                  :cy y-trans
                                  :fill (process/rgb-map-to-str rgb-map)})]
      (dom/circle (clj->js circle-props)))))
(def point-component (om/factory Point {:keyfn :point-id}))

;;
;; GridDataCell should be taking care of this one
;;
(defui Intersect
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:grid-cell/id props)])
  static om/IQuery
  (query [this]
    [:grid-cell/id :value {:tube (om/get-query gen/Location)}
     {:system-gas (om/get-query gen/SystemGas)}]))

(defui Misc
  static om/Ident
  (ident [this props]
    [:misc/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id]))

(defn random-circle []
  {
   :point-id    (rand-int 10000000)
   :x     (rand-int 200)
   :y     (rand-int 200)
   :val   (rand-int 20)})

(defn point? [in]
  (let [{:keys [x y val point-id]} in]
    (and x y val point-id)))

(defn render-points [props computed-props]
  (let [points (:points props)
        intersect (:intersect computed-props)
        _ (assert intersect "points have to be in a line")]
    #_(println "points in " (-> intersect :system-gas :short-name) " we should render: " (count points) ": " (map #(select-keys % [:x :y :val :point-id]) points))
    (for [point points]
      (point-component (om/computed point computed-props)))))

(defui Line
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :colour {:intersect (om/get-query Intersect)}
     ;{:graph/points (om/get-query Point)}
     ])
  Object
  (initLocalState [this]
    {:points []})
  (componentDidMount [this]
    (let [chan (:comms-chan (om/get-computed this))
          _ (assert chan "Must have a channel")
          _ (go-loop [ch chan]
                     (let [{:keys [cmd value line]} (<! ch)]
                       (case cmd
                         :debug-rand-point
                         (do
                           (let [new-point (random-circle)]
                             (println "Need create rand point, put it in local state, and render all the points")
                             (om/update-state! this update :points conj new-point)
                             ;(render-points this (om/get-state this) {} "componentDidMount's go loop")
                             ))
                         :new-point
                         (do
                           (println "new point: " value ", on line: " line)
                           (assert (point? value) (str "Not a point: " value))
                           (om/update-state! this update :points conj value))
                         :remove-all
                         (do
                           (println "In remove all from " (-> (om/props this) :id))
                           (om/update-state! this dissoc :points)))
                       (recur ch)))]))
  (render [this]
    (let [props (om/props this)
          {:keys [point-fn]} (om/get-computed this)
          _ (assert point-fn)
          ;_ (println "Line props:" props)
          {:keys [colour intersect graph/points]} props
          _ (assert (zero? (count points)) (str "points found in:" props))
          ;_ (println "POINTs count: " (count points))
          ]
      (dom/g nil
             (render-points (om/get-state this) {:rgb-map colour :translator point-fn :intersect intersect})))))
(def line-component (om/factory Line {:keyfn :id}))

;(point-component (om/computed point {:rgb-map colour :translator point-fn}))

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
              _ (when (not hidden?) (println "NOT HIDDEN = " (:intersect line) (:intersect current-line)))
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
        ;_ (println "x-position: " x-position)
        points (sort-by :x (:graph/points current-line))
        ]
    (when (and x-position (not-empty points))
      (let [pair (process/enclosed-by horiz-fn points x-position)
            ;_ (println "pair: " pair)
            proportionals (when pair (u/bisect-vertical-between (point-fn (first pair)) (point-fn (second pair)) x-position))
            ;_ (println "proportionals: " proportionals)
            ]
        ;(println "count x-gas-details: " (count x-gas-details))
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
    [:id :height :visible?
     ;:x-position
     ;:in-sticky-time?
     {:graph/x-gas-details (om/get-query RectTextTick)}
     {:graph/current-line (om/get-query Line)}
     ])
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [id height visible?]} props
          {:keys [horiz-fn point-fn x-position in-sticky-time?]} (om/get-computed this)
          _ (assert horiz-fn)
          _ (assert x-position "find x-position in local state of outer component")
          _ (assert (not (nil? in-sticky-time?)) "find in-sticky-time? in local state of outer component")
          ;_ (assert comms-channel)
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

;;
;; Leaving for the comments only!
;;
(defui Feeder
  static om/Ident
  (ident [this props]
    [:feeder/by-id (:id props)])
  static om/IQuery
  (query [this]
    ;;
    ;; It seems you can't just go for your own query, must go for a standard query that exists
    ;; in another component, otherwise you won't get default-db-format. Anyway from the std query
    ;; we will have no problem doing this query, as recursive denormalization will have been done:
    ;;
    ;[:id {:graph/lines [:id {:intersect [{:system-gas [:lowest :highest :long-name]}]}]}]
    ;[:id #_{:graph/lines [:id]}]
    ;;
    ;; If it really keeps on having lines then we are going to have to change our mutations
    [{:graph/lines (om/get-query Line)}]
    )
  Object
  (render [this]
    (println "Feeder doesn't render anything, it just exists!")))
(def feeder (om/factory Feeder {:keyfn :id}))

(defn- now-time [] (js/Date.))
(defn boolean? [v]
  (or (true? v) (false? v)))

(defui TrendingGraph
  static om/Ident
  (ident [this props]
    [:trending-graph/by-id (:id props)])  
  static om/IQuery
  (query [this]
    [:id
     :width 
     :height
     ;:last-mouse-moment
     {:graph/lines (om/get-query Line)}
     {:graph/navigator (om/get-query navigator/GraphNavigator)}
     ;:hover-pos
     :labels-visible?
     {:graph/misc (om/get-query Misc)}
     {:graph/plumb-line (om/get-query PlumbLine)}
     {:graph/translators [:point-fn :horiz-fn]}])
  Object
  (initLocalState [this]
    {:last-mouse-moment nil
     :x-position 0
     :hover-pos nil
     :in-sticky-time? false})
  (mouse-change [this in-sticky-time? hover-pos last-mouse-moment]
    (when (not in-sticky-time?)
      (om/update-state! this assoc :last-mouse-moment last-mouse-moment)
      (om/update-state! this assoc :x-position hover-pos))
    (om/update-state! this assoc :hover-pos hover-pos)
    (om/update-state! this assoc :in-sticky-time? in-sticky-time?))
  (sticky-change [this new-sticky]
    (om/update-state! this assoc :in-sticky-time? new-sticky))
  (handler-fn [this e]
    ;(assert comms-channel)
    (let [bounds (. (dom/node this) getBoundingClientRect)
          y (- (.-clientY e) (.-top bounds))
          x (- (.-clientX e) (.-left bounds))
          mouse-evt-type (.-type e)
          ;_ (println x y "in" comms-channel)
          ]
      ;(put! comms-channel {:type (.-type e) :x x :y y})
      (case mouse-evt-type
        "mousemove" (let [now-moment (now-time)
                          {:keys [last-mouse-moment x-position hover-pos in-sticky-time?]} (om/get-state this)
                          plumb-line (:graph/plumb-line (om/props this))
                          _ (println "in-sticky-time?, x: " in-sticky-time? x)
                          ]
                      (.mouse-change this in-sticky-time? x now-moment)
                      #_(om/transact! this `[(graph/mouse-change {:in-sticky-time? ~in-sticky-time?
                                                                :hover-pos ~x
                                                                :last-mouse-moment ~now-moment
                                                                :graph/labels-visible? false})
                                           :graph/plumb-line]))
        "mouseup" (let [in-sticky-time? (:in-sticky-time? (om/get-state this))
                        _ (assert (boolean? in-sticky-time?))
                        opposite (not in-sticky-time?)
                        ]
                    (.sticky-change this opposite)
                    #_(om/transact! this `[(graph/in-sticky-time? {:in-sticky-time? ~opposite})
                                         :graph/plumb-line]))
        "mousedown")
      nil))
  (render [this]
    (let [app-props (om/props this)
          local-props (om/get-state this)
          {:keys [in-sticky-time? x-position]} local-props
          comms-chan (chan)
          ;_ (pprint props)
          {:keys [width
                  height
                  graph/lines
                  graph/navigator
                  graph/labels-visible?
                  graph/misc graph/plumb-line graph/translators]} app-props
          _ (assert (and width height) (str "No width or height in: <" app-props ">"))
          line-chans (into {} (map (fn [line] [(:id line) (chan)]) lines))
          {:keys [point-fn horiz-fn]} translators
          _ (assert point-fn)
          _ (assert horiz-fn)
          computed-for-line (merge translators {:comms-chan comms-chan})
          computed-for-plumb (merge translators {:x-position x-position :in-sticky-time? in-sticky-time?})
          handler #(.handler-fn this %)
          handlers {:onMouseMove handler :onMouseUp handler :onMouseDown handler}
          init {:width width :height height}
          init-props (merge {:style {:border "thin solid black"}} init handlers)
          _ (go-loop [] (let [msg (<! comms-chan)
                              line-ident (-> msg :line)
                              ]
                          (if (nil? line-ident)
                            (doseq [ch (vals line-chans)]
                              (>! ch msg))
                            (if-let [target-chan (some (fn [[k v]] (when (= k (second line-ident)) v)) line-chans)]
                              (>! target-chan msg)
                              (println "Sumfin not thought of yet!"))))
                     (recur))
          ]
      (dom/div nil
               (dom/svg (clj->js init-props)
                        (for [line lines]
                          (line-component (om/computed line (merge computed-for-line {:comms-chan (some (fn [[k v]] (when (= k (:id line)) v)) line-chans)}))))
                        (plumb-line-component (om/computed (merge plumb-line init) computed-for-plumb))
                        )
               (navigator/navigator (om/computed navigator {:lines lines :comms-chan comms-chan}))))))
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


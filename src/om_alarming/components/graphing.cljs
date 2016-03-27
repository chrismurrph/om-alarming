(ns om-alarming.components.graphing
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout]]
            [om-alarming.graph.processing :as process]
            [om-alarming.util.utils :as u]
            [om-alarming.util.colours :refer [white light-blue black brown]]
            [om-alarming.components.general :as gen]
            [om-alarming.components.navigator :as navigator]
            [cljs.pprint :as pp :refer [pprint]]
            [om-alarming.reconciler :as reconciler]
            [om-alarming.components.log-debug :as ld]
            )
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
    (ld/log-render "Point" this)
    (let [props (om/props this)
          {:keys [point-id x y val]} props
          {:keys [rgb-map translator]} (om/get-computed this)
          ;_ (println "POINT: " rgb-map " " x " " y)
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

(defn calc-proportionals [horiz-fn point-fn points x-position]
  (assert (and horiz-fn point-fn))
  (assert x-position)
  (when (seq points)
    (let [pair (process/enclosed-by horiz-fn points x-position)
          ;_ (println "pair: " pair)
          proportionals (when pair (u/bisect-vertical-between (point-fn (first pair)) (point-fn (second pair)) x-position))
          ;_ (println "proportionals: " proportionals)
          ]
      proportionals)))

(defui Line
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :colour {:intersect (om/get-query Intersect)}])
  Object
  (initLocalState [this]
    {:points []})
  (componentDidMount [this]
    (let [{:keys [comms-chan horiz-fn point-fn]} (om/get-computed this)
          {:keys [id]} (om/props this)
          _ (assert (and point-fn horiz-fn))
          _ (assert comms-chan "Must have a channel")
          _ (go-loop []
                     (let [{:keys [cmd value reply-chan x-position] :as msg} (<! comms-chan)
                           ;_ (u/log (not= cmd :new-point) (str "msg is " (u/unselect-keys msg [:reply-chan]) " in " id))
                           ]
                       (case cmd
                         :debug-rand-point
                         (let [new-point (random-circle)]
                           (om/update-state! this update :points conj new-point))
                         :new-point
                         (do
                           (assert (point? value) (str "Not a point: " value))
                           (om/update-state! this update :points conj value))
                         :remove-all
                         (do
                           (u/log true (str "In remove all from " (-> (om/props this) :id)))
                           (om/update-state! this dissoc :points))
                         :request-y-at-x
                         (do
                           ;(u/log true (str "Got request points, where reply chan is: " reply-chan ", and at: " x-position))
                           (assert reply-chan)
                           (let [points (om/get-state this :points)
                                 answer (calc-proportionals horiz-fn point-fn (sort-by :x points) x-position)]
                             (async/put! reply-chan {:cmd :y-at-x-response :y-proportionals (merge answer {:line-id id :points-count (count points)})})))
                         )
                       (recur)))]))
  (render [this]
    (ld/log-render "Line" this)
    (let [props (om/props this)
          {:keys [point-fn]} (om/get-computed this)
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
    (ld/log-render "RectTextTick" this)
    (let [{:keys [id graph/line]} (om/props this)
          {:keys [current-line x testing-name proportional-y proportional-val]} (om/get-computed this)]
      (when proportional-y
        (let [;_ (println "proportional-y: " proportional-y)
              _ (assert id)
              _ (assert line (str "x-gas-info w/out a line. \nCOMPUTED:\n" (om/get-computed this)
                                  "\nPROPs:\n" (om/props this)))
              ;;; text ;;;
              _ (assert current-line (str "Not found a current-line"))
              _ (assert line (str "Which line is this for?"))
              text-colour-str (-> current-line :colour process/rgb-map-to-str)
              ;_ (println "colour will be " colour-str)
              units-str (:units current-line)
              hidden? (not= line current-line)
              ;_ (when (not hidden?) (println "NOT HIDDEN = " (:intersect line) (:intersect current-line)))
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
              line-props (merge process/line-defaults
                                {:id      proportional-y
                                 :x1      x
                                 :y1      proportional-y
                                 :x2      (+ x 6)
                                 :y2      proportional-y
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
  (let [{:keys [graph/x-gas-details x-position testing-name graph/current-line proportionals]} drop-info
        points-count (:points-count proportionals)
        _ (assert (and x-gas-details x-position current-line proportionals))
        ;_ (println "x-position: " x-position "points: " points-count)
        ]
    (when (and x-position (pos? points-count))
      (dom/g nil
             (for [x-gas-info x-gas-details]
               (rect-text-tick (om/computed x-gas-info (merge {:current-line     current-line
                                                               :x                x-position
                                                               :testing-name     testing-name
                                                               :proportional-val nil
                                                               :proportional-y   nil} proportionals))))))))

;;
;; proportionals is for the current line. Makes sense as when the current line changes we will have
;; a new PlumbLine anyway.
;; What is a bit 'wrong' is that we do say 4 rect tick texts, yet 3 of them will be invisible, only one
;; of them using the proportionals
;;
(defui PlumbLine
  static om/Ident
  (ident [this props]
    [:plumb-line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     :visible?
     {:graph/x-gas-details (om/get-query RectTextTick)}
     {:graph/current-line (om/get-query Line)}
     ])
  Object
  (initLocalState [this]
    {:last-mouse-moment nil
     :x-position 0
     :hover-pos nil
     :in-sticky-time? false
     :proportionals {:proportional-y nil :proportional-val nil} ;;-> for documentation, will be overwritten
     })
  (componentDidMount [this]
    (let [{:keys [comms-chan]} (om/get-computed this)
          _ (assert comms-chan "Must have a channel")
          _ (go-loop []
                     (let [{:keys [cmd value]} (<! comms-chan)
                           {:keys [hover-pos last-mouse-moment ys-at-x]} value
                           {:keys [in-sticky-time?]} (om/get-state this)]
                       (case cmd
                         :mouse-change
                         (do
                           (when (not in-sticky-time?)
                             (om/update-state! this assoc :last-mouse-moment last-mouse-moment)
                             (om/update-state! this assoc :x-position hover-pos))
                           (om/update-state! this assoc :hover-pos hover-pos))
                         :sticky-change
                         (let [opposite-to-current (not in-sticky-time?)]
                           (om/update-state! this assoc :in-sticky-time? opposite-to-current))
                         :ys-at-x-response
                         (let [current-id (-> (om/props this) :graph/current-line :id)
                               for-current-line (some #(when (= current-id (:line-id %)) %) ys-at-x)]
                           (u/log false (str "A one: " for-current-line))
                           (u/log false (str "A few: " ys-at-x ", when current id: " (-> (om/props this) :graph/current-line :id)))
                           (om/update-state! this assoc :proportionals for-current-line))
                         )
                       (recur)))]))
  (render [this]
    (ld/log-render "PlumbLine" this)
    (let [app-props (om/props this)
          {:keys [id visible?]} app-props
          {:keys [height] :as computed-props} (om/get-computed this)
          _ (assert height (str "Must have height, computed-props: " (select-keys computed-props [:height])))
          {:keys [in-sticky-time? x-position proportionals] :as local-state} (om/get-state this)
          _ (assert proportionals)
          stroke (process/rgb-map-to-str brown)
          opacity (if in-sticky-time? 0 1) ;; <- experimenting with not seeing the line when it is nil
          line-props (merge process/line-defaults
                            {:x1           x-position
                             :y1           0
                             :x2           x-position
                             :y2           height
                             :stroke       stroke
                             :opacity      opacity})
          res (when visible?
                (dom/g nil
                       (rect-text-ticks (merge app-props local-state computed-props))
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
    (ld/log-render "Feeder" this)
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
     {:graph/lines (om/get-query Line)}
     {:graph/navigator (om/get-query navigator/GraphNavigator)}
     :labels-visible?
     {:graph/misc (om/get-query Misc)}
     {:graph/plumb-line (om/get-query PlumbLine)}
     {:graph/translators [:point-fn :horiz-fn]}])
  Object
  (initLocalState [this]
    (println "In initLocalState for TrendingGraph")
    (let [comms (chan)
          debounce-ch (u/debounce comms 20)]
      ;;
      ;; If you want a message not to be debounced/slowed then put it into the post debounce chan
      ;;
      {:lag-chan comms
       :post-debounce-chan debounce-ch}))
  (componentDidMount [this]
    (println "In componentDidMount for TrendingGraph"))
  (mouse-change [this hover-pos last-mouse-moment]
    (let [{:keys [lag-chan post-debounce-chan]} (om/get-state this)
          msg1 {:cmd :mouse-change :value {:hover-pos hover-pos :last-mouse-moment last-mouse-moment}}
          msg2 {:cmd :request-y-at-x :x-position hover-pos :reply-chan post-debounce-chan}
          ]
      (async/put! post-debounce-chan msg1)
      ;We are going to want to do this, but debounced:
      ;Which is re-routed to all the lines
      (async/put! lag-chan msg2)))
  (sticky-change [this x-position]
    (let [local-state (om/get-state this)
          ch (:post-debounce-chan local-state)]
      (async/put! ch {:cmd :sticky-change})))
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
        "mousemove" (let [now-moment (now-time)]
                      (.mouse-change this (int x) now-moment))
        "mouseup" (let []
                    (.sticky-change this (int x)))
        "mousedown")
      nil))
  (render [this]
    (ld/log-render "TrendingGraph" this)
    (let [app-props (om/props this)
          local-props (om/get-state this)
          {:keys [lag-chan post-debounce-chan]} local-props
          ;_ (pprint props)
          {:keys [width
                  height
                  graph/lines
                  graph/navigator
                  graph/labels-visible?
                  graph/misc
                  graph/plumb-line
                  graph/translators]} app-props
          _ (assert (and width height) (str "No width or height in: <" app-props ">"))
          line-chans (into {} (map (fn [line] [(:id line) (chan)]) lines))
          plumb-chan (chan)
          {:keys [point-fn horiz-fn]} translators
          _ (assert point-fn)
          _ (assert horiz-fn)
          _ (assert plumb-line)
          ;computed-for-line (merge translators {:comms-chan post-debounce-chan})
          computed-for-plumb (merge translators {:comms-chan plumb-chan})
          handler #(.handler-fn this %)
          handlers {:onMouseMove handler :onMouseUp handler :onMouseDown handler}
          init {:width width :height height}
          init-props (merge {:style {:border "thin solid black"}} init handlers)
          _ (go-loop [proportionals [] in-sticky? false]
                     (let [msg (<! post-debounce-chan)
                           cmd (:cmd msg)
                           line-ident (-> msg :line)]
                       (cond
                         (= cmd :y-at-x-response)
                         (let [received (-> msg :y-proportionals)
                               collected (conj proportionals received)
                               ;_ (u/log true (str ":y-at-x-response. Collected: " (count collected) ", where num chans: " (count line-chans)))
                               ]
                           (if (= (count line-chans) (count collected))
                             (do
                               ;(u/log true (str "Whew, now got all:" collected))
                               (>! plumb-chan {:cmd :ys-at-x-response :value {:ys-at-x collected}})
                               (recur [] in-sticky?))
                             (recur collected in-sticky?)))

                         (= cmd :mouse-change)
                         (do
                           (>! plumb-chan msg)
                           (recur proportionals in-sticky?))

                         (= cmd :sticky-change)
                         (do
                           (>! plumb-chan msg)
                           (recur proportionals (not in-sticky?)))

                         ;;
                         ;; Only information we get from each line is to do the calculation of the proportionals.
                         ;; Only the line has the points. When stuck we don't want to get any points.
                         ;;
                         (nil? line-ident)
                         (do
                           (when (not in-sticky?)
                             (doseq [ch (vals line-chans)]
                               (>! ch msg)))
                           (recur proportionals in-sticky?))

                         :else
                         (if-let [target-line-chan (some (fn [[k v]] (when (= k (second line-ident)) v)) line-chans)]
                           (do
                             (>! target-line-chan msg)
                             (recur proportionals in-sticky?))
                           (do
                             (println "Not thought of this yet!")
                             (recur proportionals in-sticky?))))))]
      (dom/div nil
               (dom/svg (clj->js init-props)
                        (for [line lines]
                          (line-component (om/computed line (merge translators {:comms-chan (some (fn [[k v]] (when (= k (:id line)) v)) line-chans)}))))
                        (plumb-line-component (om/computed plumb-line (merge computed-for-plumb init)))
                        )
               (navigator/navigator (om/computed navigator {:lines lines :comms-chan post-debounce-chan}))))))
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


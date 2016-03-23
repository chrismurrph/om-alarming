(ns om-alarming.components.no-d3-just-svg
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
            [om-alarming.reconciler :as reconciler]
            [om-alarming.util.colours :as colours])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def point-defaults
  {:stroke (process/rgb-map-to-str black)
   ;:strokeWidth 2
   :r 5})

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
          {:keys [point-id x y]} props
          {:keys [rgb-map translator]} (om/get-computed this)
          _ (println "POINT: " rgb-map " " x " " y " will be " colours/pink)
          ;_ (assert point-id)
          _ (assert (and x y))
          ;[x-trans y-trans val-trans] (translator props)
          circle-props    (merge point-defaults
                                 {:cx x
                                  :cy y
                                  :fill (process/rgb-map-to-str colours/pink)})]
      (dom/circle (clj->js circle-props)))))
(def point-component (om/factory Point {:keyfn :point-id}))

(defn render-circles [component props src]
  (let [_ (println "Inside rendering circles from " src)
        circles (:circles props)]
    (println "Circles we should render: " circles)
    (dom/g nil
           (for [circle circles]
             (point-component circle)))))

(defn random-circle []
  {
   :point-id    (rand-int 10000000)
   :x     (rand-int 200)
   :y     (rand-int 200)
   :size  (+ 5 (rand-int 30))
   #_#_:color (case (rand-int 5)
            0 "yellow"
            1 "green"
            2 "orange"
            3 "blue"
            4 "black")})

(defn add-circle [some-component new-circle]
  (println "Going be adding" new-circle)
  (om/update-state! some-component update :circles conj new-circle)
  ;(swap! state update :circles conj (random-circle))
  )

(defui SVGThing
  Object
  ;(add-sq [] (add-circle this))
  (initLocalState [this]
    {:circles [(random-circle)]})
  (componentDidMount [this]
    (let [;initial-local-state (om/get-state this)
          chan (:chan (om/props this))
          _ (println "received is " chan)
          _ (go-loop [ch chan]
                     (let [incoming (<! ch)]
                       (add-circle this incoming)
                       (render-circles this (om/get-state this) "componentDidMount's go loop")
                       (recur ch)))]
      #_(render-circles this initial-local-state "componentDidMount")))
  #_(shouldComponentUpdate [this next-props next-state] false)
  #_(componentWillReceiveProps [this props]
    (render-circles this (om/get-state this) "componentWillReceiveProps"))
  (render [this]
    (let [initial-local-state (om/get-state this)]
      (dom/svg (clj->js {:style   {:backgroundColor "rgb(240,240,240)"}
                         :width   200 :height 200
                         :viewBox "0 0 1000 1000"})
               (render-circles this initial-local-state "SVGThing render")))))
(def svg-thing (om/factory SVGThing))

(def state-atom (atom {:circles []}))

(defui PresentDefCard
  ;static om/IQuery
  ;(query [this]
  ;  [:id :logs/name :logs/description])
  Object
  (another-circle [this some-chan]
    (fn []
      (go (>! some-chan (random-circle)))))
  (render [this]
    (let [props (om/props this)
          some-chan (chan)
          ;thing (d3-thing (merge props {:chan some-chan}))
          ]
      ;(assert (om/component? thing) "Not a component")
      (dom/div nil
               ;(dom/button #js {:onClick #(add-circle state-atom)} "Add Random circle")
               ;;(om.next/update-state! some-component update :some/key inc)
               (dom/button #js {:onClick (.another-circle this some-chan)} "Add Random circle")
               (dom/button #js {:onClick #(reset! state-atom {:circles []})} "Clear")
               (dom/br nil)
               (dom/br nil)
               (svg-thing (merge props {:chan some-chan}))))))
(def present-defcard (om/factory PresentDefCard))




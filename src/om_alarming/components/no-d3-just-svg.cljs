(ns om-alarming.components.no-d3-just-svg
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout]]
            [om-alarming.graph.processing :as process]
            [om-alarming.util.utils :as u]
            [om-alarming.util.colours :refer [white light-blue black]]
            [cljs.pprint :as pp :refer [pprint]]
            [om-alarming.util.colours :as colours])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def point-defaults
  {:stroke (process/rgb-map-to-str black)
   :r 5})

(defui Point
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [point-id x y]} props
          {:keys [rgb-map translator]} (om/get-computed this)
          _ (println "POINT: " rgb-map " " x " " y " will be " colours/pink)
          _ (assert (and x y))
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
   :size  (+ 5 (rand-int 30))})

(defn add-circle [some-component new-circle]
  (println "Going be adding" new-circle)
  (om/update-state! some-component update :circles conj new-circle))

(defn clear-circles [some-component]
  (println "Going be clearing all circles")
  (om/update-state! some-component dissoc :circles))

(defui SVGThing
  Object
  (initLocalState [this]
    {:circles [(random-circle)]})
  (componentDidMount [this]
    (let [chan (:chan (om/props this))
          _ (println "received is " chan)
          _ (go-loop [ch chan]
                     (let [{:keys [cmd value]} (<! ch)]
                       (case cmd
                         :add (do
                                (add-circle this value)
                                (render-circles this (om/get-state this) "componentDidMount's go loop"))
                         :clear (do
                                  (clear-circles this)
                                  (render-circles this (om/get-state this) "componentDidMount's go loop")))
                       (recur ch)))]))
  (render [this]
    (let [initial-local-state (om/get-state this)]
      (dom/svg (clj->js {:style   {:backgroundColor "rgb(240,240,240)"}
                         :width   200 :height 200
                         :viewBox "0 0 1000 1000"})
               (render-circles this initial-local-state "SVGThing render")))))
(def svg-thing (om/factory SVGThing))

(defui PresentDefCard
  Object
  (another-circle [this some-chan]
    (fn []
      (go (>! some-chan {:cmd :add :value (random-circle)}))))
  (clear-circles [this some-chan]
    (fn []
      (go (>! some-chan {:cmd :clear :value nil}))))
  (render [this]
    (let [props (om/props this)
          some-chan (chan)]
      (dom/div nil
               (dom/button #js {:onClick (.another-circle this some-chan)} "Add Random circle")
               (dom/button #js {:onClick (.clear-circles this some-chan)} "Clear")
               (dom/br nil)
               (dom/br nil)
               (svg-thing (merge props {:chan some-chan}))))))
(def present-defcard (om/factory PresentDefCard))
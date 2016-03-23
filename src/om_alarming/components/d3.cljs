(ns om-alarming.components.d3
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

(defn render-squares [component props src]
  (let [_ (println "Inside rendering squares from " src)
        svg (-> js/d3 (.select (dom/node component)))
        data (clj->js (:squares props))
        selection (-> svg
                      (.selectAll "rect")
                      (.data data (fn [d] (.-id d))))]
    (-> selection
        .enter
        (.append "rect")
        (.style "fill" (fn [d] (.-color d)))
        (.attr "x" "0")
        (.attr "y" "0")
        .transition
        (.attr "x" (fn [d] (.-x d)))
        (.attr "y" (fn [d] (.-y d)))
        (.attr "width" (fn [d] (.-size d)))
        (.attr "height" (fn [d] (.-size d))))
    (-> selection
        .exit
        .transition
        (.style "opacity" "0")
        .remove)
    false))

(defn random-square []
  {
   :id    (rand-int 10000000)
   :x     (rand-int 900)
   :y     (rand-int 900)
   :size  (+ 50 (rand-int 300))
   :color (case (rand-int 5)
            0 "yellow"
            1 "green"
            2 "orange"
            3 "blue"
            4 "black")})

(defn add-square [some-component new-square]
  (println "Going be adding" new-square)
  (om/update-state! some-component update :squares conj new-square)
  ;(swap! state update :squares conj (random-square))
  )

(defui D3Thing
  Object
  ;(add-sq [] (add-square this))
  (initLocalState [this]
    {:squares [(random-square)]})
  (componentDidMount [this]
    (let [initial-local-state (om/get-state this)
          chan (:chan (om/props this))
          _ (println "received is " chan)
          _ (go-loop [ch chan]
                     (let [incoming (<! ch)]
                       (add-square this (random-square))
                       (recur ch)))]
      (render-squares this initial-local-state "componentDidMount")))
  (shouldComponentUpdate [this next-props next-state] false)
  (componentWillReceiveProps [this props]
    (render-squares this (om/get-state this) "componentWillReceiveProps"))
  (render [this]
    (dom/svg (clj->js {:style   {:backgroundColor "rgb(240,240,240)"}
                       :width   200 :height 200
                       :viewBox "0 0 1000 1000"}))))
(def d3-thing (om/factory D3Thing))

(def state-atom (atom {:squares []}))

(defui PresentDefCard
  ;static om/IQuery
  ;(query [this]
  ;  [:id :logs/name :logs/description])
  Object
  (another-square [this some-chan]
    (fn []
      (println "Need do thru channel: " some-chan)
      (go (>! some-chan (random-square)))))
  (render [this]
    (let [props (om/props this)
          some-chan (chan)
          ;thing (d3-thing (merge props {:chan some-chan}))
          ]
      ;(assert (om/component? thing) "Not a component")
      (dom/div nil
               ;(dom/button #js {:onClick #(add-square state-atom)} "Add Random Square")
               ;;(om.next/update-state! some-component update :some/key inc)
               (dom/button #js {:onClick (.another-square this some-chan)} "Add Random Square")
               (dom/button #js {:onClick #(reset! state-atom {:squares []})} "Clear")
               (dom/br nil)
               (dom/br nil)
               (d3-thing (merge props {:chan some-chan}))))))
(def present-defcard (om/factory PresentDefCard))

#_(defn present-defcard []
  (dom/div nil
           (dom/button #js {:onClick #(add-square state-atom)} "Add Random Square")
           (dom/button #js {:onClick #(reset! state-atom {:squares []})} "Clear")
           (dom/br nil)
           (dom/button #js {:onClick #(pprint @state-atom)} "Print State")
           (dom/br nil)
           (d3-thing @state-atom)))

(comment
  (defcard sample-d3-component
           (fn [state-atom _]
             (dom/div nil
                      (dom/button #js {:onClick #(add-square state-atom)} "Add Random Square")
                      (dom/button #js {:onClick #(reset! state-atom {:squares []})} "Clear")
                      (dom/br nil)
                      (dom/br nil)
                      (d3-thing @state-atom)))
           {:squares []}
           {:inspect-data true}))



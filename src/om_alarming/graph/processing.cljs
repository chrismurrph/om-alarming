(ns om-alarming.graph.processing
  (:require [goog.string :as gstring]
            [goog.string.format]
            [om-alarming.util.utils :as u :refer [log distance bisect-vertical-between]]
            [om-alarming.graph.mock-values :refer [black]]
            [om-alarming.reconciler :as reconciler]
            [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [cljs.core.match.macros :refer [match]]))

;;
;; Anything that originally comes from graphing that does have to do with Reagent or the state
;;

(defn- rgb-map-to-str [{r :r g :g b :b}]
  (str "rgb(" r "," g "," b ")"))

(defn- format-as-str [dec-pl val units]
  (gstring/format (str "%." dec-pl "f" units) val))

(defn- now-time [] (js/Date.))
(defn- seconds [js-date] (/ (.getTime js-date) 1000))

(def line-defaults
  {:stroke (rgb-map-to-str black)
   :strokeWidth 1})

(def point-defaults
  {;:stroke (rgb-map-to-str black)
   ;:stroke-width 2
   :r 2})

(defn find-line [my-lines name]
  (first (filter #(= name (:name %)) my-lines)))

;;
;; current-label used to be (:current-label @state)
;;
(defn- hidden? [line-id current-label]
  (assert line-id)
  (let [current (:name current-label)
        _ (assert current)
        res (not= current line-id)
        ;_ (when res (println "Hidden b/c not equal: " current line-id))
        ]
    res))

;(def default-init-state {:translator nil})
;(def init-state (atom default-init-state))

(defn- staging-translators [min-x min-y max-x max-y graph-width graph-height]
  (let [horiz-trans-fn (fn [val] (u/scale {:min min-x :max max-x} {:min 0 :max graph-width} val))
        vert-trans-fn (fn [val] (u/scale {:min min-y :max max-y} {:min 0 :max graph-height} val))
        trans-point-fn (fn [[x [y val]]] [(horiz-trans-fn x) (vert-trans-fn y) val])
        ]
    {:horiz-fn horiz-trans-fn :vert-fn vert-trans-fn :point-fn trans-point-fn}))

;; These work, depending on how well the reads are implemented
;; (in-ns 'om-alarming/graph.processing)
;;
;;(my-parser {:state my-reconciler} [{:app/selected-button [:name] }])
;;(my-parser {:state my-reconciler} [{:app/buttons [:name] }])
;;
;; How we can get a simple bit of top level state
;;
;;(my-parser {:state my-reconciler} '[[:graph/in-sticky-time? _]])

(defn- controller [inchan]
  (go-loop [cur-x nil cur-y nil old-x nil old-y nil]
           (match [(<! inchan)]

                  [{:type "mousemove" :x x :y y}]
                  (let [now-moment (now-time)
                        in-sticky-time? (reconciler/top-level-query :in-sticky-time?)
                        diff (distance [old-x old-y] [cur-x cur-y])
                        is-flick (> diff 10)]
                    (when (not is-flick)
                      (when (not in-sticky-time?)
                        (reconciler/alteration 'graph/mouse-change
                                               {:graph/hover-pos x :graph/last-mouse-moment now-moment :graph/labels-visible? false}
                                               :trending)
                        ;(println "HOVER:" (reconciler/query :graph/hover-pos))
                        ))
                    (recur x y cur-x cur-y))

                  ;[{:type "mouseup" :x x :y y}]
                  ;(let [current-line (dec (my-lines-size))]
                  ;  (log "Already colour of current line at " current-line " is " (get-in @state [:my-lines current-line :colour]))
                  ;  (swap! state update-in [:my-lines current-line :points] (fn [points-at-n] (vec (conj points-at-n [x y]))))
                  ;  ;(u/log "When mouse up time is: " when-last-moved)
                  ;  (recur x y old-x old-y))

                  [_]
                  (do
                    (recur cur-x cur-y old-x old-y)))))

;;
;; keyword options:
;; :height :width :trans-point :get-positions :get-colour
;; All are defaulted - see main-component
;; Note that :trans-colour does not exist - colours have to be of shape {:r :g :b}
;;
(defn init []
  (let [ch (chan)
        proc (controller ch)
        options-map (reconciler/top-level-query :graph/init)
        _ (println "Created a controller, width is " (:width options-map))
        misc (into {:comms ch} options-map)
        _ (println "S/be going into graph/misc: " misc)
        staging (:staging options-map)
        graph-width (:width options-map)
        _ (assert graph-width ":width needs to be supplied at init")
        graph-height (:height options-map)
        _ (assert graph-height ":height needs to be supplied at init")
        translators (staging-translators (or (:min-x staging) 0) (or (:min-y staging) 0) (or (:max-x staging) 999)
                                         (or (:max-y staging) 999) graph-width graph-height)
        ]
    (reconciler/alteration 'graph/translators {:translators translators} :graph/translators)
    (reconciler/alteration 'graph/misc {:misc misc} :graph/misc)
    ;; Leaving in for curiosity
    (go
      (let [exit (<! proc)]
        (prn :exit! exit)))))
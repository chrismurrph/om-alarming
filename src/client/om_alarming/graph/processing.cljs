(ns om-alarming.graph.processing
  (:require [goog.string :as gstring]
            [goog.string.format]
            [om-alarming.util.utils :as u :refer [log distance bisect-vertical-between]]
            [om-alarming.util.colours :refer [black]]
            ;[om-alarming.reconciler :as reconciler]
            [cljs.pprint :refer [pprint]]
            [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [cljs.core.match.macros :refer [match]]))

;;
;; Any x may have two positions, one on either side, or none. These two positions will be useful to the drop-down
;; y-line that comes up as the user moves the mouse over the graph.
;; In the reduce implementation we only know the previous one when we have gone past it, hence we need to keep the
;; prior in the accumulator.
;; Because of the use-case, when we are exactly on it we repeat it. I'm thinking the two values will have the greatest
;; or least used. This obviates the question of there being any preference for before or after. Also when user is at
;; the first or last point there will still be a result.
;;
(defn enclosed-by [translate-horizontally-fn points x]
  (let [;_ (println "Num points to reduce over: " (count points))
        ;_ (pprint points)
        res (reduce (fn [acc ele] (if (empty? (:res acc))
                                    (let [cur-x (translate-horizontally-fn (:x ele))]
                                      (if (= cur-x x)
                                        {:res [ele ele]}
                                        (if (> cur-x x)
                                          {:res [(:prev acc)] :prev ele} ;use the prior element
                                          {:res [] :prev ele} ;only update prior element
                                          )
                                        )
                                      )
                                    (let [result-so-far (:res acc)]
                                      (if (= 1 (count result-so-far))
                                        {:res (conj result-so-far (:prev acc))}
                                        acc)
                                      )
                                    ))
                    []
                    points)
        ]
    (let [result (:res res)]
      ;(println "enclosed-by RES: " result "when looked thru:" (count points) "points")
      (if (nil? (first result)) ;when are before first element
        nil
        (if (empty? result)
          nil
          (if (= 1 (count result))
            (let [last-ele (last points)]
              (conj result last-ele))
            result))))))

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
   :strokeWidth 1
   :opacity 1})

(def point-defaults
  {;:stroke (rgb-map-to-str black)
   ;:stroke-width 2
   :r 2})

(defn find-line [my-lines name]
  (first (filter #(= name (:name %)) my-lines)))

;;
;; current-label used to be (:current-label @state)
;;
;(defn- hidden? [line-id current-label]
;  (assert line-id)
;  (let [current (:name current-label)
;        _ (assert current)
;        res (not= current line-id)
;        ;_ (when res (println "Hidden b/c not equal: " current line-id))
;        ]
;    res))

;(def default-init-state {:translator nil})
;(def init-state (atom default-init-state))

(defn- staging-translators [min-x min-y max-x max-y graph-width graph-height]
  (let [horiz-trans-fn (fn [val] (u/scale {:min min-x :max max-x} {:min 0 :max graph-width} val))
        vert-trans-fn (fn [val] (u/scale {:min min-y :max max-y} {:min 0 :max graph-height} val))
        trans-point-fn (fn [point]
                         (let [_ (assert (map? point))
                               {:keys [x y val]} point
                               _ (assert (and x y val))]
                           [(horiz-trans-fn x) (vert-trans-fn y) val]))
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

;;
;; keyword options:
;; :height :width :trans-point :get-positions :get-colour
;; All are defaulted - see main-component
;; Note that :trans-colour does not exist - colours have to be of shape {:r :g :b}
;;
(defn init []
  (let [;ch (chan)
        ;proc (controller ch)
        options-map (:graph/trending-graph (reconciler/internal-query [{:graph/trending-graph [:width :height]}]))
        _ (println "Created a controller, back: " options-map)
        ;misc (into {:comms ch} options-map)
        ;_ (println "S/be going into graph/misc: " misc)
        staging (:staging options-map)
        graph-width (:width options-map)
        _ (assert graph-width ":width needs to be supplied at init")
        graph-height (:height options-map)
        _ (assert graph-height ":height needs to be supplied at init")
        translators (staging-translators (or (:min-x staging) 0) (or (:min-y staging) 0) (or (:max-x staging) 999)
                                         (or (:max-y staging) 999) graph-width graph-height)
        ]
    (println "About to do mutates")
    (reconciler/alteration 'graph/translators {:translators translators} [:graph/trending-graph :graph/translators])
    ;(reconciler/alteration 'graph/misc {:misc misc} :graph/misc)
    ))
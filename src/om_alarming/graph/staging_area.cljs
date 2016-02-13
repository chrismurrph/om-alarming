(ns om-alarming.graph.staging-area
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout pipe onto-chan]]
            [om-alarming.util.utils :refer [log no-log abs]]
            [om-alarming.graph.mock-values :refer [light-blue green pink]]
            [om-alarming.util.utils :as u])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

;;
;; The x and y coming in here are in 'our' co-ordinate system (i.e. not the graph's one). The scale function already
;; knows about 'our' co-ordinate system. And the two functions horizontally-translate and vertically-translate know
;; how to scale across from one system to another, going TO the graph's geometry. Our geometry happens to be 0-999
;; for both x and y. You can only see that if you look at the source of db.
;;
;(def translate-point (fn [{x :x y :y val :val}] [(horizontally-translate x) (vertically-translate y) val]))
;(def translator {:vertically vertically-translate :horizontally horizontally-translate :whole-point translate-point})
(def line-keys [:name :units :colour :dec-places])

;;
;; Although they say lowest and highest they actually mean lowest and highest thresholds
;; (a higher threshold means that it is worse, which may mean it has a lower value)
;;
(def gas-infos [{:name "Carbon Dioxide" :lowest 0.5 :highest 1.35}
                {:name "Carbon Monoxide" :lowest 30 :highest 55}
                {:name "Oxygen" :lowest 19 :highest 12}
                {:name "Methane" :lowest 0.25 :highest 1}])

;;
;; Given lowest and highest work out a divider so that given a change of 1 in the business
;; metric (gas for example) we can get a corresponding change in the y of this staging area.
;; Here we are assuming the staging height is 1000, and saying half that (500) should show this
;; huge change from lowest to highest. We can divide any business change by the result to get
;; the 'pixels'.
;; (By pixels I mean units of staging area - just easier to think of as pixels)
;;
(defn transition-divide-by [lowest highest]
  (let [spread (abs (- highest lowest))
        five-hundreth (/ spread 500)]
    five-hundreth))

(defn stage-ify-changer [lowest highest]
  (let [divide-num (transition-divide-by lowest highest)]
  (fn [central-y external-val]
    (let [external-over-central (- external-val central-y)
          stage-val-over-central (quot external-over-central divide-num)
          stage-val (+ stage-val-over-central 499)]
      stage-val))))

;(log "Pixels position: " ((stage-ify-changer 19 12) 22 19))

;;
;; transitioner will convert an external y value into a stage y value. The resulting function will be converting one
;; point into another one. It will be used as part of a transducer which pumps values onto the trending graph
;;
(defn release-point [transitioner time->x]
  (fn [point-map-in]
    (let [business-value (:val point-map-in)
          stage-value (transitioner business-value)
          stage-x (time->x (:time point-map-in))
          res (merge point-map-in {:point [stage-x stage-value business-value]})]
      res)))

;;
;; start and end in actual time that to be made between 0 -> 999 inclusive
;;
(defn calc-x-from-time [start end]
  (assert start)
  (assert end)
  (let [from-world {:min start :max end}
        to-world {:min 0 :max 999}]
    (fn [time]
      (let [_ (assert (>= time start) (str "time: " time " not gte start: " start))
            _ (assert (< time end) (str "time: " time " not lt end: " end))
            res (u/scale from-world to-world time)]
        res))))

;;
;; Talking about the middle of along the x or time axis, where the middle is say the middle 10%
;;
(defn in-middle? [proportion start end]
  (let [full-length (- end start)
        half-length (/ full-length 2)
        centre-x (+ start half-length)
        half-proportion (/ (* full-length proportion) 2)
        start-middle (- centre-x half-proportion)
        end-middle (+ centre-x half-proportion)
        ;_ (log "start " start)
        ;_ (log "end " end)
        ;_ (log "start-middle " start-middle)
        ;_ (log "end-middle " end-middle)
        ]
    (fn [x]
      (and (>= x start-middle) (<= x end-middle)))))

;;
;; Receives raw business trend data and transforms it so it will be positioned correctly on this stage
;; (which is close to being positioned properly on the graph itself)
;;
(defn receiver [name time->x central? out-chan in-chan]
  (let [{:keys [lowest highest]} (first (filter (fn [info] (= name (-> info :name))) gas-infos))
        _ (log name " " lowest " " highest)
        transitioner (stage-ify-changer lowest highest)]
    (go-loop [accumulated []
              release-channel nil]
             (let [data-in (<! in-chan)]
               (if (nil? release-channel)
                 (if (central? (:time data-in))
                   (let [central-y (:val data-in)
                         new-transitioner (partial transitioner central-y)
                         release-point-fn (release-point new-transitioner time->x)
                         new-release-transducer (map release-point-fn)
                         new-release-channel (chan 1 new-release-transducer)
                         _ (pipe new-release-channel out-chan)]
                     (onto-chan new-release-channel accumulated false)
                     (>! new-release-channel data-in)
                     (recur [] new-release-channel))
                   (recur (conj accumulated data-in) release-channel))
                 (do
                   (>! release-channel data-in)
                   (recur [] release-channel))))))
  in-chan)

;;
;;
;;
;(defn create [lines]
;  ""
;  (g/remove-all-lines)
;  (doseq [line lines]
;    (g/add-line (select-keys line line-keys))))

(defn show
  ""
  [lines start end in-chan]
  (assert in-chan)
  (let [out-chan (chan)
        names (map :name lines)
        receiving-chans (into {} (map (fn [name] (vector name (chan))) names))
        central? (in-middle? 0.1 start end)
        time->x (calc-x-from-time start end)
        receivers (into {} (map (fn [[name chan]] (vector name (receiver name time->x central? out-chan chan))) receiving-chans))]
    (go-loop []
             (let [latest-val (<! in-chan)
                   its-name (:name latest-val)
                   _ (no-log "name from incoming: " its-name)
                   receiving-chan (get receivers its-name)
                   _ (assert receiving-chan (str "Not found receiving channel for " its-name " from " receivers))
                   _ (>! receiving-chan latest-val)])
             (recur))
    out-chan))


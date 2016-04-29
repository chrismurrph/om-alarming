(ns om-alarming.parsing.mutations.graph
  (:require [om.next :as om]
            ;[om-alarming.reconciler :refer [mutate]]
            [untangled.client.mutations :as m]
            [cljs-time.core :as time]
            [om-alarming.components.log-debug :as ld]))

#_(defn as-mouse-changes [orig-state params]
  (let [{:keys [hover-pos last-mouse-moment in-sticky-time?]} params
        ;_ (assert (db-format/boolean? in-sticky-time?))
        ]
    (let [st (if (not in-sticky-time?)
               (-> orig-state
                   (assoc-in [:trending-graph/by-id 10300 :last-mouse-moment] last-mouse-moment)
                   (assoc-in [:plumb-line/by-id 10201 :x-position] hover-pos))
               orig-state)]
      (-> st
          (assoc-in [:trending-graph/by-id 10300 :hover-pos] hover-pos)
          (assoc-in [:plumb-line/by-id 10201 :in-sticky-time?] in-sticky-time?)
          ;(assoc-in [:drop-info/by-id 10200 :x] hover-pos)
          ))))

#_(defmethod mutate 'graph/mouse-change
  [{:keys [state]} _ params]
  {:value  {:keys [:graph/trending-graph]}
   :action #(swap! state as-mouse-changes params)})

#_(defmethod mutate 'graph/in-sticky-time?
  [{:keys [state]} _ params]
  {:value  {:keys [:in-sticky-time?]}
   :action #(swap! state assoc-in [:plumb-line/by-id 10201 :in-sticky-time?] (:in-sticky-time? params))})

(defn insert-translators [state translators]
  ;(println "INS: " (-> translators :point-fn))
  (swap! state assoc-in [:translators/by-id 11000] (merge {:id 11000} translators)))

(defmethod m/mutate 'graph/translators
  [{:keys [state]} k {:keys [translators]}]
  (ld/log-mutation k)
  {:value  {:keys [:graph/trending-graph :graph/translators]}
   :action #(insert-translators state translators)})

(defmethod  m/mutate 'graph/misc
  [{:keys [state]} k params]
  (ld/log-mutation k)
  {:value  {:keys [:graph/misc]}
   ;:action #(swap! state assoc :graph/misc misc)
   :action #(swap! state assoc-in [:misc/by-id 10400] (merge {:id 10400} (:misc params)))})

(defmethod  m/mutate 'graph/toggle-receive
  [{:keys [state]} k _]
  (ld/log-mutation k)
  {:value  {:keys [:receiving?]}
   :action #(swap! state update-in [:navigator/by-id 10600 :receiving?] not)})

;(defmethod mutate 'graph/start-receive
;  [{:keys [state]} _ _]
;  {:value  {:keys [:receiving?]}
;   :action #(swap! state update-in [:navigator/by-id 10600 :receiving?] true)})

;(defmethod mutate 'graph/stop-receive
;  [{:keys [state]} _ _]
;  {:value  {:keys [:receiving?]}
;   :action #(swap! state assoc-in [:graph/navigator 10600 :receiving?] false)})

;;
;; Will have to reduce over every line ident with state being the accumulator
;;
#_(defn rm-all-points-from-line [st line-ident]
  (-> st
      (assoc-in (conj line-ident :graph/points) [])))

#_(defn rm-all-points [st]
  (let [all-lines-idents (:graph/lines st)]
    (-> (reduce rm-all-points-from-line
                st
                all-lines-idents)
        (assoc :graph/points []))))

(defn update-end-time [st fn]
  (-> st
      (update-in [:navigator/by-id 10600 :end-time] fn)
      ;(rm-all-points)
      (assoc-in [:navigator/by-id 10600 :receiving?] false)))

(defn assoc-end-time [st new-val]
  (-> st
      (assoc-in [:navigator/by-id 10600 :end-time] new-val)
      ;(rm-all-points)
      (assoc-in [:navigator/by-id 10600 :receiving?] false)))

(defmethod  m/mutate 'navigate/forwards
  [{:keys [state]} k {:keys [seconds]}]
  (ld/log-mutation k)
  {:action #(swap! state update-end-time (fn [end-time] (time/plus end-time (time/seconds seconds))))})

(defmethod  m/mutate 'navigate/backwards
  [{:keys [state]} k {:keys [seconds]}]
  (ld/log-mutation k)
  {:action #(swap! state update-end-time (fn [end-time] (time/minus end-time (time/seconds seconds))))})

(defmethod  m/mutate 'navigate/now
  [{:keys [state]} k _]
  (ld/log-mutation k)
  {:action #(swap! state assoc-end-time (time/now))})

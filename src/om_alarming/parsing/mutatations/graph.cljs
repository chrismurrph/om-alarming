(ns om-alarming.parsing.mutations.graph
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]
            [default-db-format.core :as db-format]))

(defn as-mouse-changes [orig-state params]
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
          (assoc-in [:drop-info/by-id 10200 :x] hover-pos)))))

(defmethod mutate 'graph/mouse-change
  [{:keys [state]} _ params]
  {:value  {:keys [:graph/trending-graph]}
   :action #(swap! state as-mouse-changes params)})

(defmethod mutate 'graph/in-sticky-time?
  [{:keys [state]} _ params]
  {:value  {:keys [:in-sticky-time?]}
   :action #(swap! state assoc-in [:plumb-line/by-id 10201 :in-sticky-time?] (:in-sticky-time? params))})

(defn insert-translators [state translators]
  ;(println "INS: " (-> translators :point-fn))
  (swap! state assoc-in [:trending-graph/by-id 10300 :graph/translators] translators))

(defmethod mutate 'graph/translators
  [{:keys [state]} _ {:keys [translators]}]
  {:value  {:keys [:graph/trending-graph :graph/translators]}
   :action #(insert-translators state translators)})

(defmethod mutate 'graph/misc
  [{:keys [state]} _ params]
  {:value  {:keys [:graph/misc]}
   ;:action #(swap! state assoc :graph/misc misc)
   :action #(swap! state assoc-in [:misc/by-id 10400] (merge {:id 10400} (:misc params)))})

(defmethod mutate 'graph/receiving-chan
  [{:keys [state]} _ {:keys [receiving-chan]}]
  {:value  {:keys [:graph/misc]}
   :action #(swap! state assoc-in [:misc/by-id 10400 :receiving-chan] receiving-chan)})

(defmethod mutate 'graph/toggle-receive
  [{:keys [state]} _ _]
  {:value  {:keys [:receiving?]}
   :action #(swap! state update-in [:trending-graph/by-id 10300 :receiving?] not)})

(defmethod mutate 'graph/stop-receive
  [{:keys [state]} _ _]
  {:value  {:keys [:trending-graph/by-id :receiving?]}
   :action #(swap! state assoc-in [:graph/trending-graph 10300 :receiving?] false)})

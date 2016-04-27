(ns om-alarming.new-core
  (:require om-alarming.parsing.mutations.app               ;; just example
            [untangled.client.core :as uc]
            [om-alarming.query :as q]
            [om-alarming.state :as state]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [om.next :as om]
            [untangled.client.logging :as log]
            [cljs.pprint :refer [pprint]])
  (:import goog.History))

(def merged-state (atom (merge state/already-normalized-tabs-state (om/tree->db q/non-union-part-of-root-query state/initial-state true))))

(defn- arm-tab [st table k ident]
  (swap! st update-in table assoc k ident))

(defn transforms [st]
  (-> st
      (arm-tab [:app/trending :singleton] :grid/gas-query-grid [:gas-query-grid/by-id 10800])))

(defonce app (atom (uc/new-untangled-client
                     ; passing an atom, since have hand normalized it already.
                     :initial-state merged-state
                     :started-callback (fn [app]
                                         #_(configure-routing! (:reconciler app))
                                         #_(let [h (History.)]
                                           (events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
                                           (doto h (.setEnabled true)))))))

(def my-reconciler (:reconciler app))



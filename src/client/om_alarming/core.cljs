(ns om-alarming.core
  (:require
    [untangled.client.core :as uc]
    [om-alarming.query :as q]
    [om-alarming.state :as state]
    [goog.events :as events]
    [goog.history.EventType :as EventType]
    [om.next :as om]
    [untangled.client.logging :as log]
    [cljs.pprint :refer [pprint]]
    [om-alarming.graph.processing :as p]
    [om-alarming.sente-client :as client]
    [om-alarming.system :as system])
  (:import goog.History))

(def merged-state (atom (merge state/already-normalized-tabs-state (om/tree->db q/non-union-part-of-root-query state/initial-state true))))

(defonce app (atom (uc/new-untangled-client
                     ; passing an atom, since have hand normalized it already.
                     :initial-state merged-state
                     :started-callback (fn [app]
                                         (let [rec (:reconciler app)
                                               par (:parser app)
                                               system-start-fn (partial system/start! par rec)]
                                           (p/init rec par)
                                           (client/start! rec)
                                           (om/transact! rec `[(graph/misc {:misc {:system-going-fn ~system/going?
                                                                                   :system-start-fn ~system-start-fn
                                                                                   :system-stop-fn  ~system/stop!}})]))))))

(defn my-reconciler-available? []
  (:reconciler @app))

(defn my-reconciler []
  (let [rec (:reconciler @app)
        _ (assert rec "No reconciler available")]
    rec))

(defn my-parser []
  (let [par (:parser @app)
        _ (assert par "No parser available")]
    par))



(ns om-alarming.new-core
  (:require                                                 ;om-alarming.parsing.mutations.app => have put all in root, not really sure where they should go
    [untangled.client.core :as uc]
    [om-alarming.query :as q]
    [om-alarming.state :as state]
    [goog.events :as events]
    [goog.history.EventType :as EventType]
    [om.next :as om]
    [untangled.client.logging :as log]
    [cljs.pprint :refer [pprint]]
    [om-alarming.graph.processing :as p]
    [om-alarming.sente-client :as client])
  (:import goog.History))

(def merged-state (atom (merge state/already-normalized-tabs-state (om/tree->db q/non-union-part-of-root-query state/initial-state true))))

(declare my-reconciler)
(defonce app (atom (uc/new-untangled-client
                     ; passing an atom, since have hand normalized it already.
                     :initial-state merged-state
                     :started-callback (fn [app]
                                         (p/init (:reconciler app) (:parser app))
                                         (client/start! (:reconciler app))))))

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



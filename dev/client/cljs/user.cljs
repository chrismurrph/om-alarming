(ns cljs.user
  (:require
    [cljs.pprint :refer [pprint]]
    [devtools.core :as devtools]
    [om-alarming.core :as core]
    [om-alarming.reconciler :as reconciler]
    ;untangled-todomvc.mutations
    ))

(enable-console-print!)

(defonce cljs-build-tools
  (do (devtools/enable-feature! :sanity-hints)
      (devtools.core/install!)))

(println "In cljs.user ns")
;(def mounted (core/mount))

(defn log-app-state
  "Helper for logging the app-state, pass in top-level keywords from the app-state and it will print only those
  keys and their values."
  [& keywords]
  (pprint (let [app-state @reconciler/my-reconciler]
            (if (= 0 (count keywords))
              app-state
              (select-keys app-state keywords)))))



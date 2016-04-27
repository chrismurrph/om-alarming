(ns cljs.user
  (:require
    [cljs.pprint :refer [pprint]]
    [devtools.core :as devtools]
    ;[om-alarming.reconciler :as reconciler]
    ;untangled-todomvc.mutations
    [om-alarming.new-core :as new-core]
    [untangled.client.core :as uc]
    [om-alarming.root :as root]))

(enable-console-print!)

(defonce cljs-build-tools
  (do (devtools/enable-feature! :sanity-hints)
      (devtools.core/install!)))

(println "In cljs.user ns")
;(def mounted (core/mount))

(println "S/see app in main area now")

(defn log-app-state
  "Helper for logging the app-state, pass in top-level keywords from the app-state and it will print only those
  keys and their values."
  [& keywords]
  (pprint (let [app-state (:reconciler @new-core/app)]
            (if (= 0 (count keywords))
              app-state
              (select-keys app-state keywords)))))



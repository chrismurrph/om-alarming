(ns om-alarming.reconciler
  (:require [om.next :as om]
            [om-alarming.new-core :as core]))

;;
;; This file destined to be removed - no need for these calls
;;

(defn top-level-query
  "Only use for top level keywords. Returns the actual value rather than a hashmap"
  [kw]
  ;(println "External query for " kw)
  (let [res ((core/my-parser) {:state (core/my-reconciler)} `[[~kw _]])]
    (when (not-empty res) (apply val res))))

(defn internal-query
  "Just invokes the read. Caller to deal with the hashmap that is returned??"
  [query]
  (let [res ((core/my-parser) {:state (core/my-reconciler)} query)]
    res))

;; (alteration 'app/tab {:new-id 1} :app/selected-button)
(defn alteration
  ([mutate-key param-map kw]
   (om/transact! (core/my-reconciler) `[(~mutate-key ~param-map) ~kw]))
  ([mutate-key param-map]
   (om/transact! (core/my-reconciler) `[(~mutate-key ~param-map)]))
  )

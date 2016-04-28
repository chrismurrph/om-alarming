(ns om-alarming.reconciler)

(defn alteration
  ([one two]
   (println "alteration: " one two))
  ([one two three]
   (println "alteration: " one two three)))

(defn internal-query [one]
  (println "internal-query" one))

;; (in-ns 'om-alarming.reconciler)
(defn top-level-query
  "Only use for top level keywords. Returns the actual value rather than a hashmap"
  [kw]
  ;(println "External query for " kw)
  (let [res (my-parser {:state my-reconciler} `[[~kw _]])]
    (when (not-empty res) (apply val res))))

(defn internal-query
  "Just invokes the read. Caller to deal with the hashmap that is returned??"
  [query]
  ;(println "External query for " kw)
  (let [res (my-parser {:state my-reconciler} query)]
    res))

;; (alteration 'app/tab {:new-id 1} :app/selected-button)
(defn alteration
  ([mutate-key param-map kw]
   (om/transact! my-reconciler `[(~mutate-key ~param-map) ~kw]))
  ([mutate-key param-map]
   (om/transact! my-reconciler `[(~mutate-key ~param-map)]))
  )

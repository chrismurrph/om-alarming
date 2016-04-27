(ns om-alarming.reconciler)

(defn alteration
  ([one two]
   (println "alteration: " one two))
  ([one two three]
   (println "alteration: " one two three)))

(defn internal-query [one]
  (println "internal-query" one))

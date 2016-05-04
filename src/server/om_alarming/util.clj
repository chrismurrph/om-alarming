(ns om-alarming.util
  (:require [clj-time.core :as t]))

(defn millis-ahead-utc []
  (let [tz (t/default-time-zone)
        utc-tz (t/time-zone-for-id "UTC")
        now (t/now)
        time-now-where-we-are (t/from-time-zone now tz)
        time-now-utc (t/from-time-zone now utc-tz)
        advance-of-utc (t/interval time-now-where-we-are time-now-utc)
        millis (.toDurationMillis advance-of-utc)
        ]
    #_(println (.toString tz))
    #_(println (.toString utc-tz))
    #_(println "Advanced by: " (-> millis
                                   (/ 1000)
                                   (/ 60)
                                   (/ 60)))
    millis))

;; http://sids.github.io/nerchuko/utils-api.html
(defn unselect-keys
  "Opposite of select-keys: returns a map containing only those
entries whose key is not in keys."
  [m keyseq]
  (select-keys m
               (clojure.set/difference (set (keys m))
                           keyseq)))

;; https://gist.github.com/micmarsh/bcbe19c9de8bb7a471bf
(defn flip [function]
  (fn
    ([] (function))
    ([x] (function x))
    ([x y] (function y x))
    ([x y z] (function z y x))
    ([a b c d] (function d c b a))
    ([a b c d & rest]
     (->> rest
          (concat [a b c d])
          reverse
          (apply function)))))

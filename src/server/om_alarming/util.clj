(ns om-alarming.util)

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

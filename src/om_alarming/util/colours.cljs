(ns om-alarming.util.colours
  (:require [clojure.set :as set]
            [om-alarming.util.utils :as u]))

(def black {:r 0 :g 0 :b 0})
(def brown {:r 165 :g 96 :b 57}) ;; -> (def brown {:r 102 :g 51 :b 0})
(def white {:r 255 :g 255 :b 255})
(def blue {:r 0 :g 51 :b 102})
(def light-blue {:r 0 :g 204 :b 255})
(def very-light-blue {:r 235 :g 244 :b 245})
(def pink {:r 255 :g 0 :b 255})
(def green {:r 0 :g 102 :b 0})
(def red {:r 255 :g 0 :b 0})
(def gray {:r 64 :g 64 :b 64})

(def all-line-colours #{black blue light-blue very-light-blue pink brown green red gray})

(defn new-random-colour
  [existing-colours]
  (assert (set? existing-colours) (str "Not a set but a " (type existing-colours)))
  (println "Existing: " existing-colours)
  (let [remaining (vec (set/difference all-line-colours existing-colours))]
    (when (seq remaining) (rand-nth remaining))))

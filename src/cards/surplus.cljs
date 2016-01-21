(ns cards.surplus
  (:require [om-alarming.components.graphing :as graph]
            [om-alarming.graph.mock-values :as data])
  (:require-macros
    [devcards.core :as dc :refer [defcard]]))

;; We won't need ticklines, in fact will be getting rid many components altogether
;;
;(defcard tick-lines
;         (fn [props _] (graph/simple-svg-tester @props))
;         {:id 2
;          :test-props {:testing-name "tick-lines"
;                       :drop-infos (merge-testing-name drop-infos "tick-lines")
;                       :visible? true}}
;         {:inspect-data false}
;         )

(defcard insert-texts
         (fn [props _] (graph/simple-svg-tester @props))
         {:id 3
          :test-props {:testing-name "insert-texts"
                       :drop-infos (merge-testing-name drop-infos "insert-texts")}}
         {:inspect-data false}
         )

(defcard backing-rects
         (fn [props _] (graph/simple-svg-tester @props))
         {:id 4
          :test-props {:testing-name "backing-rects"
                       :drop-infos (merge-testing-name drop-infos "backing-rects")}}
         {:inspect-data false}
         )

;;; x y line-id current-label
(defcard opaque-rect
         (fn [props _] (graph/simple-svg-tester @props))
         {:id 5
          :test-props {:testing-name  "opaque-rect"
                       :x 50
                       :proportional-y 50
                       :name 1
                       :current-label {:name 1 :dec-places 1}}}
         {:inspect-data false}
         )

;;; old
;;; x y-intersect colour-str txt-with-units line-id current-label
;;; new
;;; x proportional-y proportional-val dec-places name my-lines current-label testing-name
(defcard text-component
         (fn [props _] (graph/simple-svg-tester @props))
         {:id         7
          :test-props {:testing-name     "text-component"
                       :x                200
                       :proportional-y   50
                       :proportional-val 0.1
                       :name             "Oxygen"
                       :my-lines         data/my-lines
                       :current-label    {:name "Oxygen" :dec-places 1}}}
         )



(ns cards.cards
  (:require [om.next :as om]
            [om.dom :as dom :include-macros true]
            [om-alarming.components.nav :as nav]
            [om-alarming.components.grid :as grid]
            [om-alarming.components.graphing :as graph]
            [om-alarming.graph.mock-values :as data]
            [cards.util :refer [render-cb-info update-cb-info]])
  (:require-macros
    [devcards.core :as dc :refer [defcard]]))

(enable-console-print!)

;;
;; Each drop-info s/be denormalised, so have more than this. Also have:
;; :x :current-label
;;
(def simple-drop-infos [{:name "Carbon Dioxide", :proportional-y 146.33422462612975, :proportional-val 0.19667279430464207 :id "Carbon Dioxide"}
                        {:name "Carbon Monoxide", :proportional-y 131.68775824757364, :proportional-val 11.337551649514731 :id "Carbon Monoxide"}
                        {:name "Oxygen", :proportional-y 161.68775824757364, :proportional-val 10.337551649514731 :id "Oxygen"}
                        ])

(def drop-infos (mapv #(merge {:x 50 :dec-places 1 :current-label {:name "Carbon Monoxide"} :my-lines data/my-lines} %) simple-drop-infos))

(defn merge-testing-name [drop-infos name]
  (mapv #(merge {:testing-name name} %) drop-infos))

(defcard point
         (fn [props _] (graph/simple-svg-tester @props))
         {:id 1
          :test-props {:testing-name "point"
                       :rgb-map data/light-blue
                       :x 50
                       :y 50}}
         {:inspect-data false}
         )

(defcard tick-lines
         (fn [props _] (graph/simple-svg-tester @props))
         {:id 2
          :test-props {:testing-name "tick-lines"
                       :drop-infos (merge-testing-name drop-infos "tick-lines")
                       :visible? true}}
         {:inspect-data false}
         )

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

;; x y line-id current-label
(defcard opaque-rect
         (fn [props _] (graph/simple-svg-tester @props))
         {:id 5
          :test-props {:testing-name  "opaque-rect"
                       :x 50
                       :proportional-y 50
                       :name 1
                       :current-label {:name 1}}}
         {:inspect-data false}
         )

;; height visible? x-position in-sticky-time?
(defcard plumb-line
  (fn [props _] (graph/simple-svg-tester @props))
  {:id 6
   :test-props {:testing-name "plumb-line"
                :height 100
                :visible? true
                :x-position 10
                :in-sticky-time? true}}
  )

;; old
;; x y-intersect colour-str txt-with-units line-id current-label
;; new
;; x proportional-y proportional-val dec-places name my-lines current-label testing-name
(defcard text-component
  (fn [props _] (graph/simple-svg-tester @props))
  {:id 7
   :test-props {:testing-name "text-component"
                :x 200
                :proportional-y 50
                :proportional-val 0.1
                :dec-places 1
                :name "Oxygen"
                :my-lines data/my-lines
                :current-label {:name "Oxygen"}}}
  )

(defcard navbar-buttons
  (fn [props _] (nav/menubar @props))
  {:id 10 :text "Navbar with buttons going across"
   :buttons [{:id 1 :name "First" :description "First Title"}
             {:id 2 :name "Second" :description "Second Title"}
             {:id 3 :name "Third" :description "Third Title" :selected true}
             {:id 4 :name "Fourth" :description "Fourth Title" :showing false}
             {:id 5 :name "Fifth" :description "Fifth Title"}
             {:id 6 :name "Sixth" :description "Sixth Title"}
             {:id 7 :name "Seventh" :description "Seventh Title"}]}
  {:inspect-data false})

(defcard selection-grid
  (fn [props _] (grid/gas-selection-grid @props))
  {:id 11 :text "Selection Grid"
   :tubes    [{:id    1
               :gases [{:id  1
                        :gas :methane
                        :selected true}
                       {:id  2
                        :gas :oxygen}
                       {:id  3
                        :gas :carbon-monoxide}
                       {:id  4
                        :gas :carbon-dioxide}
                       ]}
              {:id    2
               :gases [{:id  1
                        :gas :methane}
                       {:id  2
                        :gas :oxygen}
                       {:id  3
                        :gas :carbon-monoxide}
                       {:id  4
                        :gas :carbon-dioxide}
                       ]}
              {:id    3
               :gases [{:id  1
                        :gas :methane}
                       {:id  2
                        :gas :oxygen}
                       {:id  3
                        :gas :carbon-monoxide}
                       {:id  4
                        :gas :carbon-dioxide
                        :selected true}
                       ]}
              {:id    4
               :gases [{:id  1
                        :gas :methane}
                       {:id  2
                        :gas :oxygen
                        :selected true}
                       {:id  3
                        :gas :carbon-monoxide}
                       {:id  4
                        :gas :carbon-dioxide}
                       ]}]}
  {:inspect-data false})

(defcard checked-checkbox
  (fn [props _] (grid/checkbox @props))
  {:id 12 :text "Checked checkbox"
   :test-props {:id  1
                :gas :methane
                :selected true
                :full-name "Arbitary Cb"}}
  {:inspect-data false})
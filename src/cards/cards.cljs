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
(def simple-x-gas-details
  [
   {:name "Carbon Dioxide", :proportional-y 146.33422462612975, :proportional-val 0.19667279430464207 :id 1}
   {:name "Carbon Monoxide", :proportional-y 131.68775824757364, :proportional-val 11.337551649514731 :id 2}
   {:name "Oxygen", :proportional-y 161.68775824757364, :proportional-val 10.337551649514731 :id 3}
   ])

(def x-gas-details (mapv #(merge {:x 50 :current-label {:name "Carbon Monoxide" :dec-places 1} :my-lines data/my-lines} %) simple-x-gas-details))

(defn merge-testing-name [drop-infos test-name]
  (mapv #(merge {:testing-name test-name} %) drop-infos))

(defcard many-rect-text-tick
         (fn [props _] (graph/simple-svg-tester @props))
         {:id 29
          :test-props {:testing-name "many-rect-text-tick"
                       :drop-info {:x 50
                                   :my-lines data/my-lines
                                   :current-label {:name "Carbon Monoxide" :dec-places 1}
                                   :x-gas-details (merge-testing-name simple-x-gas-details "many-rect-text-tick")}}}
         {:inspect-data false}
         )

(defcard rect-text-tick
         (fn [props _] (graph/simple-svg-tester @props))
         {:id 30
          :test-props {:height 500 ;; not effective
                       :width 200
                       :testing-name "rect-text-tick"
                       :x-gas-info (merge {:testing-name "rect-text-tick"} (nth simple-x-gas-details 0))
                       :current-label {:name "Carbon Dioxide" :dec-places 2}
                       :x 120
                       :id 1
                       :my-lines data/my-lines}}
         {:inspect-data false}
         )

;
;;; height visible? x-position in-sticky-time?
(defcard plumb-line
  (fn [props _] (graph/simple-svg-tester @props))
  {:id 6
   :test-props {:testing-name "plumb-line"
                :height 100
                :visible? true
                :x-position 10
                :in-sticky-time? true}}
  )

(defcard navbar-buttons
  (fn [props _] (nav/menubar @props))
  {:id 10 :text "Navbar with buttons going across"
   :app/buttons [{:id 1 :name "First" :description "First Title"}
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
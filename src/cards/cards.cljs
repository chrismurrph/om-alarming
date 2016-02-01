(ns cards.cards
  (:require [om.next :as om]
            [om.dom :as dom :include-macros true]
            [om-alarming.components.nav :as nav]
            [om-alarming.components.surplus :as surplus]
            [om-alarming.components.grid :as grid]
            [om-alarming.components.graphing :as graph]
            [om-alarming.graph.mock-values :as data]
            [cards.util :refer [render-cb-info update-cb-info]])
  (:require-macros
    [devcards.core :as dc :refer [defcard]]))

(enable-console-print!)

;;
;; Does the same thing as om/computed
;;
(defn om-computed [props computed-props]
  (merge props {:om.next/computed computed-props}))

;;
;; Each drop-info s/be denormalised, so have more than this. Also have:
;; :x :current-label
;;
(def simple-x-gas-details
  [
   {:name "Carbon Dioxide at 2", :proportional-y 146.33422462612975, :proportional-val 0.19667279430464207 :id 1}
   {:name "Carbon Monoxide at 3", :proportional-y 131.68775824757364, :proportional-val 11.337551649514731 :id 2}
   {:name "Oxygen at 4", :proportional-y 161.68775824757364, :proportional-val 10.337551649514731 :id 3}
   ])

(def x-gas-details (mapv #(merge {:x 50 :current-label {:name "Carbon Monoxide" :dec-places 1} :my-lines data/my-lines} %) simple-x-gas-details))

(defn merge-testing-name [drop-infos test-name]
  (mapv #(merge {:testing-name test-name} %) drop-infos))

(defcard main-component
         (fn [props _] (graph/main-component @props))
         {:id 28
          :text "main-component"
          :graph/init {:width 640
                       :height 250}}
         {:inspect-data false})


(defcard many-rect-text-tick
         (fn [props _] (graph/simple-svg-tester @props))
         {:id 29
          :test-props
              {:testing-name "many-rect-text-tick"
               :drop-info (om/computed {:x 50
                                        :lines data/my-lines
                                        :current-label {:name "Carbon Monoxide at 3" :dec-places 1}
                                        :x-gas-details (merge-testing-name simple-x-gas-details "many-rect-text-tick")
                                        :testing-name "many-rect-text-tick"}
                                       {})
               }}
         {:inspect-data false}
         )

(defcard rect-text-tick
         (fn [props _] (graph/simple-svg-tester @props))
         {:id 30
          :test-props
              (om-computed (merge {:height 500
                                   :width  200
                                   :id     1} (first (map #(merge {:testing-name "rect-text-tick"} %) simple-x-gas-details)))
                           {:testing-name  "rect-text-tick"
                            :current-label {:name "Carbon Dioxide at 2" :dec-places 2}
                            :x             120
                            :lines   data/my-lines})}
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
   :app/selected-button {:id 3}
   :app/buttons [{:id 1 :name "First" :description "First Title"}
             {:id 2 :name "Second" :description "Second Title"}
             {:id 3 :name "Third" :description "Third Title"}
             {:id 4 :name "Fourth" :description "Fourth Title" :showing false}
             {:id 5 :name "Fifth" :description "Fifth Title"}
             {:id 6 :name "Sixth" :description "Sixth Title"}
             {:id 7 :name "Seventh" :description "Seventh Title"}]}
  {:inspect-data false})

(defcard selection-grid
  (fn [props _] (grid/gas-selection-grid @props))
  {:id 11 :text "Selection Grid"
   :app/gases [{:id 1 :short-name "CH4"} {:id 2 :short-name "O2"} {:id 3 :short-name "CO"} {:id 4 :short-name "CO2"}]
   :app/tubes    [{:id    1
               :tube/gases [{:id  1
                        :system-gas {:short-name "CH4"}
                        :selected true}
                       {:id  2
                        :system-gas {:short-name "O2"}}
                       {:id  3
                        :system-gas {:short-name "CO"}}
                       {:id  4
                        :system-gas {:short-name "CO2"}}
                       ]}
              {:id    2
               :tube/gases [{:id  5
                       :system-gas {:short-name "CH4"}}
                       {:id  6
                        :system-gas {:short-name "O2"}}
                       {:id  7
                        :system-gas {:short-name "CO"}}
                       {:id  8
                        :system-gas {:short-name "CO2"}}
                       ]}
              {:id    3
               :tube/gases [{:id  9
                       :system-gas {:short-name "CH4"}}
                       {:id  10
                        :system-gas {:short-name "O2"}}
                       {:id  11
                        :system-gas {:short-name "CO"}}
                       {:id  12
                        :system-gas {:short-name "CO2"}
                        :selected true}
                       ]}
              {:id    4
               :tube/gases [{:id  13
                       :system-gas {:short-name "CH4"}}
                       {:id  14
                        :system-gas {:short-name "O2"}
                        :selected true}
                       {:id  15
                        :system-gas {:short-name "CO"}}
                       {:id  16
                        :system-gas {:short-name "CO2"}}
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

(defcard md-submit-button
         (fn [props _] (surplus/md-submit-button @props))
         {}
         {:inspect-data false}
         )
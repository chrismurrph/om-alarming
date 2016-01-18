(ns cards.cards
  (:require [devcards.core :as dc :refer-macros [defcard]]
            [om.next :as om]
            [om.dom :as dom :include-macros true]
            [om-alarming.components.nav :as nav]
            [om-alarming.components.grid :as grid]
            [cards.util :refer [render-cb-info update-cb-info]]))

(enable-console-print!)


(defcard
  "## Om Alarming cards

   Examples demonstrating how Alarming cards look given different properties
   or layout constraints.")

(defcard
  "### Navbar buttons"
  (fn [props _] (nav/menubar @props))
  {:id 1 :text "Navbar with buttons going across"
   :buttons [{:id 1 :name "First" :description "First Title"}
             {:id 2 :name "Second" :description "Second Title"}
             {:id 3 :name "Third" :description "Third Title" :selected true}
             {:id 4 :name "Fourth" :description "Fourth Title" :showing false}
             {:id 5 :name "Fifth" :description "Fifth Title"}
             {:id 6 :name "Sixth" :description "Sixth Title"}
             {:id 7 :name "Seventh" :description "Seventh Title"}]}
  {:inspect-data false})

(defcard
  "### Graphing selection Grid"
  (fn [props _] (grid/gas-selection-grid @props))
  {:id 2 :text "Selection Grid"
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

(defcard
  "### A checked CheckBox"
  (fn [props _] (grid/checkbox @props))
  {:id 1 :text "Checked checkbox"
   :test-props {:id  1
                :gas :methane
                :selected true
                :full-name "Arbitary Cb"}}
  {:inspect-data false})
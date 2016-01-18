(ns cards.cards
  (:require [devcards.core :as dc :refer-macros [defcard]]
            [om.next :as om]
            [om.dom :as dom :include-macros true]
            [om-alarming.components.nav :as n]
            [cards.util :refer [render-cb-info update-cb-info]]))

(enable-console-print!)


(defcard
  "## Om Alarming cards

   Examples demonstrating how Alarming cards look given different properties
   or layout constraints.")

(defcard
  "### Navbar buttons"
  (fn [props _] (n/menubar @props))
  {:id 1 :text "Navbar with buttons going across"
   :buttons [{:id 1 :name "First" :description "First Title"}
             {:id 2 :name "Second" :description "Second Title"}
             {:id 3 :name "Third" :description "Third Title" :selected true}
             {:id 4 :name "Fourth" :description "Fourth Title" :showing false}
             {:id 5 :name "Fifth" :description "Fifth Title"}
             {:id 6 :name "Sixth" :description "Sixth Title"}
             {:id 7 :name "Seventh" :description "Seventh Title"}]}
  {:inspect-data true})
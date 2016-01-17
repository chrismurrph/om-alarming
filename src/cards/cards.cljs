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
   :buttons [{:id 1 :name "First"}
             {:id 2 :name "Second"}
             {:id 3 :name "Third"}]}
  {:inspect-data true})
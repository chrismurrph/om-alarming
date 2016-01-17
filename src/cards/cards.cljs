(ns cards.cards
  (:require [devcards.core :as dc :refer-macros [defcard]]
            [om.next :as om]
            [om.dom :as dom]
            [om-alarming.components.nav :as n]
            [cards.util :refer [render-cb-info update-cb-info]]))

(enable-console-print!)


(defcard
  "## Om Alarming cards

   Examples demonstrating how Alarming cards look given different properties
   or layout constraints.")

(defcard
  "### Navbar buttons"
  (fn [props _] (n/navbar @props))
  {:id 1 :text "Card --- with two assignees"
   :buttons [{:id 2 :username "ada" :name "Ada Lovelace"}
             {:id 3 :username "zuse" :name "Konrad Zuse"}]}
  {:inspect-data true})
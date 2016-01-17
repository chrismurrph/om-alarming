(ns cards.cards
  (:require [devcards.core :as dc :refer-macros [defcard]]
            [om.next :as om]
            [om.dom :as dom]
            ;[om-alarming.components.card :as kanban-card]
            [cards.util :refer [render-cb-info update-cb-info]]))

(enable-console-print!)

(defcard
  "## Om Alarming cards

   Examples demonstrating how Alarming cards look given different properties
   or layout constraints.")
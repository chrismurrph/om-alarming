(ns kanban.tree-db
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pp :refer [pprint]]))

(def initial-state
  {:boards
   [{:id 1
     :name "Development"
     :description
     (str "Kanban board for developers. Developers select work "
          "from Issues and the Backlog. When they start working, "
          "they move these cards to doing and assign them to "
          "themselves. Once completed, they move the card to "
          "Test for testers to verify the results of the work.")
     :lanes [{:id 10} {:id 11} {:id 12} {:id 13}]
     :secret {:id 2000}}
    {:id 2
     :name "Testing"
     :description
     (str "Kanban board for testers. Testers pick up the work that "
          "developers have completed and moved to Test. They then "
          "move these cards to Testing and assign them to themselves. "
          "If testing is successful, cards are moved to Done, otherwise "
          "they are moved to Issues, where they are again picked up by "
          "developers for another round.")
     :lanes [{:id 13} {:id 14} {:id 15} {:id 10}]}]
   :lanes
   [{:id 10 :name "Issues" :cards [{:id 102}]}
    {:id 11 :name "Backlog" :cards [{:id 100} {:id 101}]}
    {:id 12 :name "Doing" :cards [{:id 103}]}
    {:id 13 :name "Test" :cards [{:id 105}]}
    {:id 14 :name "Testing" :cards [{:id 104}]}
    {:id 15 :name "Done" :cards [{:id 106}]}]
   :cards
   [{:id 100 :text "Allow users to edit card assignees"}
    {:id 101 :text "Allow users to change lane names"}
    {:id 102 :text "Fix card drag-and-drop on IE8 and Firefox"}
    {:id 103 :text "Implement basic Kanban demo app"
     :assignees [{:id 1000}]}
    {:id 104 :text "Figure out how to deploy the demo app on Heroku"
     :assignees [{:id 1000} {:id 1001}]}
    {:id 105 :text "Write instructions on how to run the demo app"
     :assignees [{:id 1001}]}
    {:id 106
     :text "Create GitHub repository for the demo app"}]
   :users
   [{:id 1000 :username "konrad" :name "Konrad Zuse"}
    {:id 1001 :username "ada" :name "Ada Lovelace"}]
   :secrets
   [{:id 2000 :name "First Secret"}{:id 2001 :name "Second Secret"}]})

(defui Secret
  static om/Ident
  (ident [this props]
    [:secret/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name]))

(defui Assignee
  static om/Ident
  (ident [this props]
    [:user/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :username :name]))

(defui Card
  static om/Ident
  (ident [this props]
    [:card/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :text {:assignees (om/get-query Assignee)}]))

(defui Lane
  static om/Ident
  (ident [this props]
    [:lane/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name {:cards (om/get-query Card)}]))

(defui Board
  static om/Ident
  (ident [this props]
    [:board/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name :description {:secret (om/get-query Secret)} {:lanes (om/get-query Lane)}]))

;;
;; Not implemented. As these methods do not need to be called this should not matter.
;; Still it is worth visiting http://localhost:3449, and looking in the Dev Tools
;; Console - but expect problems. In fact you need to go there and refresh before
;; calling (show-db).
;;
(defmulti read om/dispatch)
(defmulti mutate om/dispatch)
(def parser
  (om/parser {:read read
              :mutate mutate}))

(def reconciler
  (om/reconciler {:normalize true ;; Documentation
                  :state initial-state
                  :parser parser}))

;; Just:
;; (in-ns 'kanban/tree-db)
;; (show-db)
;; from the REPL you started with `lein figwheel test`
(defn show-db []
  (pprint @reconciler)
  nil
  )

(defui App
  static om/IQuery
  (query [this]
    [{:boards (om/get-query Board)}
     {:boards/active (om/get-query Board)}
     {:lanes (om/get-query Lane)}
     {:secrets (om/get-query Secret)}
     {:cards (om/get-query Card)}
     :cards/dragged
     {:users (om/get-query Assignee)}]))

(defn run []
  (om/add-root! reconciler
                App
                (.. js/document (getElementById "main-app-area"))))

(run)

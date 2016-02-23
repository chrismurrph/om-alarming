(ns question.sessions
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :refer [pprint]]))

(def init-data
  {:app/session {:id 1
                 :app/messages [{:id 100}]}
   :app/messages [{:id 100 :text "Message 1"}
                  {:id 101 :text "Message 2"}]
   :app/users [{:id 200 :email "1@foo.com"}
               {:id 201 :email "2@foo.com"}]})

(defui Message
  static om/Ident
  (ident [this {:keys [id]}]
    [:message/by-id id])
  static om/IQuery
  (query [this]
    [:id :text]))

(defui User
  static om/Ident
  (ident [this {:keys [id]}]
    [:user/by-id id])
  static om/IQuery
  (query [this]
    [:id :email]))

(defui Session
  static om/Ident
  (ident [this {:keys [id]}]
    [:session/by-id id])
  static om/IQuery
  (query [this]
    [:id {:app/messages (om/get-query Message)}]))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)
(def parser
  (om/parser {:read read
              :mutate mutate}))

(def my-reconciler
  (om/reconciler {:normalize true ;; Documentation
                  :state init-data
                  :parser parser}))

(defn show-db
  "(in-ns 'question.sessions)
   (show-db)
   from the REPL you started with `lein figwheel test`"
  []
  (pprint @my-reconciler)
  nil)

(defui RootView
  static om/IQuery
  (query [this]
    [{:app/messages (om/get-query Message)}
     {:app/users (om/get-query User)}
     {:app/session (om/get-query Session)}]))

(defn run []
  (om/add-root! my-reconciler
                RootView
                (.. js/document (getElementById "main-app-area"))))

(run)

;(def norm-data (om/tree->db RootView init-data true))

;{:session [:user/by-id 1],
; :messages [[:message/by-id 1] [:message/by-id 2]],
; :users [[:user/by-id 1] [:user/by-id 2]],
; :message/by-id
; {1 {:message/id 1, :text "Message 1"},
;  2 {:message/id 2, :text "Message 1"}},
; :user/by-id
; {1 {:user/id 1, :email "1@foo.com", :messages [{:message/id 1}]},
;  2 {:user/id 2, :email "2@foo.com"}},
; :om.next/tables #{:message/by-id :user/by-id}}


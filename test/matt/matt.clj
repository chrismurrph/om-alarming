(ns matt.matt
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pp :refer [pprint]]
            [cljs.test :refer-macros :as dc :refer [deftest is testing run-tests]]))

(defui Child
       static om/Ident
       (ident [_ props]
              [:db/id (get props :db/id)])
       static om/IQuery
       (query [_]
              [:db/id :name]))

(defui Children
       static om/IQuery
       (query [_]
              [{:kids (om/get-query Child)}]))

(defui Parent
       static om/IQuery
       (query [_]
              [{[:children '_] (om/get-query Children)}]))

(defui App
       static om/IQuery
       (query [this]
              [:parent (om/get-query Parent)]))

(dc/deftest app-test
            (testing "App"
                     (testing "tree->db"
                              (om/get-query App)
                              (let [r (om/tree->db App {:parent {:children {:kids [{:db/id 1 :name "matt"} {:db/id 2 :name "chris"}]}}} true)]
                                (is (= r {:parent {:children [:children '_]}
                                          :children [[:db/id 1] [:db/id 2]]
                                          :db/id {1 {:db/id 1 :name "matt"} 2 {:db/id 2 :name "chris"}}
                                          :om.next/tables #{:db/id}
                                          }))))
                     (comment
                       {:parent
                                        {:children
                                         {:kids [{:db/id 1, :name "matt"} {:db/id 2, :name "chris"}]}},
                        :om.next/tables #{}})
                     (testing "db->tree"
                              (let [st {:parent {:children [:children '_]}
                                        :children {:kids [[:db/id 1] [:db/id 2]]}
                                        :db/id {1 {:db/id 1 :name "matt"} 2 {:db/id 2 :name "chris"}}}
                                    r (om/db->tree (om/get-query App) st st)]
                                (is (= r {:parent {:children [{:db/id 1 :name "matt"} {:db/id 2 :name "chris"}]}}))))
                     (comment
                       {:parent {:children [:children _]}})
                     ))

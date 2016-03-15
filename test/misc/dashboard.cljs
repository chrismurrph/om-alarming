(ns misc.dashboard
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :refer [pprint]]
            [default-db-format.core :as db-format]))

;;
;; https://clojurians.slack.com/files/tomjack/F0SFT7XPZ/dashboard_dashboard_cljs.clj
;;
;; Seems like by-id is not good enough for union stuff. Here the by-ids will be:
;;
;; :post/comment
;; and
;; :dashboard/post
;;
;; , so at the very least need to support multiple "by-id". Here "comment" and "post"
;; I think the problem might be that in his data he just has :comments - but I need to find that out using the tool.
;;

;;; trimmed down unions tutorial, with post comments added

(def union-init-data
  {:dashboard/items
   [{:id 0 :type :dashboard/post
     :author "Laura Smith"
     :title "A Post!"
     :content "Lorem ipsum dolor sit amet, quem atomorum te quo"
     :favorites 0
     :post/comments [{:id 0 :text "a comment"}]}]})  ;; <- To pass need `:post/comments` rather than `:comments`
​
(defui Comment
  static om/Ident
  (ident [this props]
    [:post/comment (:id props)])
  static om/IQuery
  (query [this]
    [:id :text]))
​
(defui Post
  static om/IQuery
  (query [this]
    [:id :type :title :author :content
     {:post/comments (om/get-query Comment)}]))
​
(defui DashboardItem
  static om/Ident
  (ident [this {:keys [id type]}]
    [type id])
  static om/IQuery
  (query [this]
    (zipmap
      [:dashboard/post]
      (map #(conj % :favorites)
           [(om/get-query Post)]))))
​
(defui Dashboard
  static om/IQuery
  (query [this]
    [{:dashboard/items (om/get-query DashboardItem)}]))
​
(enable-console-print!)

(let [supposedly-norm (om/tree->db Dashboard union-init-data true)
      chk-res (db-format/check {:by-id-kw ["comment" "post"] :excluded-keys #{:om.next/tables}} supposedly-norm)
      okay? (db-format/ok? chk-res)]
  (if (not okay?)
    (do
      (println chk-res) ;; <- chk-res is usually picked up by components i.e. shown in the HUD
      (pprint supposedly-norm))
    (println "All is in default db format")))

(comment
  ;; gives this:
  {:dashboard/items [[:dashboard/post 0]],
   :dashboard/post
                    {0
                     {:id 0,
                      :type :dashboard/post,
                      :author "Laura Smith",
                      :title "A Post!",
                      :content "Lorem ipsum dolor sit amet, quem atomorum te quo",
                      :favorites 0,
                      :comments [{:id 0, :text "a comment"}]}},
   :om.next/tables #{:dashboard/post}}

  ;; hoped for this:
  {:dashboard/items [[:dashboard/post 0]],
   :dashboard/post
                    {0
                     {:id 0,
                      :type :dashboard/post,
                      :author "Laura Smith",
                      :title "A Post!",
                      :content "Lorem ipsum dolor sit amet, quem atomorum te quo",
                      :favorites 0,
                      :comments [[:post/comment 0]]}},
   :post/comment
                    {0 {:id 0, :text "a comment"}}
   :om.next/tables #{:dashboard/post :post/comment}})

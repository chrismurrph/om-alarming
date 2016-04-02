(ns misc.autocomplete
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :refer [pprint]])
  (:import [goog Uri]
           [goog.net Jsonp]))

(enable-console-print!)

(def base-url
  "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")

(defn jsonp
  ([uri] (jsonp (chan) uri))
  ([c uri]
   (let [gjsonp (Jsonp. (Uri. uri))]
     (.send gjsonp nil #(put! c %))
     c)))

(defmulti read om/dispatch)

(defmethod read :search/results
  [{:keys [state ast] :as env} k {:keys [user-query]}]
  (println "state:" @state)
  (println "ast:" ast)
  (println "query:" user-query)
  (println "---------")
  (merge
    {:value (get @state k [])}
    (when-not (or (string/blank? user-query)
                  (< (count user-query) 3))
      {:search ast})))

(def my-parser (om/parser {:read read}))

;;
;; There might be something in the here that we are loosing
;; It is not just the query but its parameters we need to
;; transfer onwards
;;
(defmethod read :the-query
  [{:keys [query] :as env} k _]
  {:value {:the-query (my-parser env query :search/results)}})

(defn result-list [results]
  (dom/ul #js {:key "result-list"}
          (map #(dom/li #js{:key %} %) results)))

(defn search-field [ac u-query]
  (dom/input
    #js {:key "search-field"
         :value u-query
         :onChange
         (fn [e]
           (let [evt-val (.. e -target -value)
                 _ (println "Entered: " evt-val)]
             (om/set-query! ac
                            {:params {:user-query evt-val}})))}))

(defui AutoCompleter
  static om/IQueryParams
  (params [_]
    {:user-query ""})
  static om/IQuery
  (query [_]
    '[(:search/results {:user-query ?user-query})])
  Object
  (render [this]
    (let [{:keys [search/results]} (om/props this)
          _ (println results)]
      (dom/div nil
               (dom/h2 nil "AutoCompleter")
               (cond->
                 [(search-field this (:user-query (om/get-params this)))]
                 (not (empty? results)) (conj (result-list results)))))))
(def auto-completer (om/factory AutoCompleter))

(defui Root
  ;static om/IQueryParams
  ;(params [_]
  ;  {:query ""})
  static om/IQuery
  (query [_]
    [{:the-query (om/get-query AutoCompleter)}])
  Object
  (render [this]
    (let [{:keys [the-query]} (om/props this)]
      (auto-completer the-query))))

(defn search-loop [c]
  (go
    (loop [[query cb] (<! c)]
      (let [[_ results] (<! (jsonp (str base-url query)))]
        (cb {:search/results results}))
      (recur (<! c)))))

(defn send-to-chan [c]
  (fn [{:keys [search]} cb]
    (when search
      (let [{[search] :children} (om/query->ast search)
            query (get-in search [:params :user-query])]
        (put! c [query cb])))))

(def send-chan (chan))

(def reconciler
  (om/reconciler
    {:state   {:search/results []}
     :parser  my-parser
     :send    (send-to-chan send-chan)
     :remotes [:remote :search]}))

(search-loop send-chan)

(om/add-root! reconciler AutoCompleter
              (gdom/getElement "main-app-area"))

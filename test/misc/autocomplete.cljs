(ns misc.autocomplete
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :refer [pprint]]
            [om-alarming.util.utils :as u])
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
  (println "user-query:" user-query)
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
(defmethod read :root-join
  [{:keys [query parser ast] :as env} k params]
  {:value (u/probe "read res" {:root-join (parser env query)})})

(defn result-list [results]
  (dom/ul #js {:key "result-list"}
          (map #(dom/li #js{:key %} %) results)))

(defn search-field [component u-query]
  (dom/input
    #js {:key   "search-field"
         :value u-query
         :onChange
                (fn [e]
                  (let [evt-val (.. e -target -value)
                        _ (println "Entered: " evt-val)]
                    (om/set-query! component
                                   {:params {:user-query evt-val}})))}))

(def app-state (atom {:search/results []}))

(declare read-query)

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
          _ (println "results:" results)]
      (dom/div nil
               (dom/h2 nil "AutoCompleter")
               (cond->
                 [(search-field this (:user-query (om/get-params this)))]
                 (not (empty? results)) (conj (result-list results)))
               (dom/br nil)
               (dom/button #js{:onClick (fn [_] (read-query))} "Read Query")))))
(def auto-completer (om/factory AutoCompleter))

(defui Root
  static om/IQuery
  (query [_]
    [{:root-join (om/get-query AutoCompleter)}])
  Object
  (render [this]
    (let [{:keys [root-join]} (om/props this)
          _ (println "root-join is:" root-join)]
      (dom/div nil
               (auto-completer root-join)
               (dom/br nil)))))

;;
;; Outside world gets back to us. Provide with channel that this go block grabs message out from.
;; Message will be a pair: left side is input, which is a query
;;                         right side is where result is to go to (cb)
;;
(defn search-loop [c]
  (go
    (loop [[user-query cb] (<! c)]
      (let [[_ results] (<! (jsonp (str base-url user-query)))]
        (cb {:search/results results}))
      (recur (<! c)))))

;;
;; The returned fn here is what ON calls when decides to send
;; Because :search is a remote, it will fill it in with what needs to go to the remote
;; It is our job to actually call the remote. See search-loop
;; ON provides the cb so we can plonk the results back into the state
;; We want to use a form of the query that jsonp can handle
;;
;; [(:search/results {:user-query chr})]
;; (om/query->ast search) =>
;;
;; {:type :root,
;;  :children
;;  [{:type         :prop,
;;    :dispatch-key :search/results,
;;    :key          :search/results,
;;    :params       {:user-query "chr"}}]
;; }
;;
(defn send-to-chan [c]
  (fn [{:keys [search]} cb]
    (when search
      (let [_ (println "WANT happening: " search)
            _ (pprint (om/query->ast search))
            {[search] :children} (om/query->ast search)
            user-query (get-in search [:params :user-query])]
        (put! c [user-query cb])))))

(def send-chan (chan))

(def my-reconciler
  (om/reconciler
    {:state   app-state #_{:search/results []}
     :parser  my-parser
     :send    (send-to-chan send-chan)
     :remotes [:remote :search]}))

(defn read-query []
  (let [q '[(:search/results {:user-query "boo"})]
        _ (om/set-query! (om/class->any my-reconciler AutoCompleter) {:params {:user-query "boo"}})
        res (my-parser {:state app-state} q)]
    (println "RES: " res)))

(search-loop send-chan)

(om/add-root! my-reconciler AutoCompleter
              (gdom/getElement "main-app-area"))

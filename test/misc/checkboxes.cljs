(ns misc.checkboxes
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [default-db-format.core :as db-format]
            [cljs.pprint :refer [pprint]]
            [om-alarming.util.utils :as u]
            [om-alarming.misc.help :as help]))

(enable-console-print!)

(def init-state
  {:graph/selected-lines
   [{:id 100}
    {:id 101}]
   :graph/lines
   [{:id   100
     :name "Methane"}
    {:id   101
     :name "Oxygen"}
    {:id   102
     :name "Carbon Dioxide"}
    {:id   103
     :name "Carbon Monoxide"}
    ]
   }
  )

(defn check-default-db [st]
  (let [version db-format/version
        check-result (db-format/check st)
        ok? (db-format/ok? check-result)
        msg-boiler (str "normalized (default-db-format ver: " version ")")
        message (if ok?
                  (str "GOOD: state fully " msg-boiler)
                  (str "BAD: state not fully " msg-boiler))]
    (println message)
    (when (not ok?)
      (pprint st)
      (db-format/show-hud check-result))))

(defui Checkbox
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name])
  Object
  (render [this]
    (let [{:keys [id name]} (om/props this)
          {:keys [selected?]} (om/get-computed this)
          _ (println "Rendering cb:" id "when selected is:" selected?)]
      (dom/div #js {:className "switch demo3"}
               (dom/input #js{:type    "checkbox"
                              :checked selected?
                              :onClick (fn [e]
                                         (let [action (.. e -target -checked)]
                                           (println "Pressed so attempting to set to:" action)
                                           (om/transact! this `[(graph/select-line {:want-to-select? ~action :id ~id}) :graph/lines])))})
               (dom/label nil (dom/i nil))))))
(def checkbox (om/factory Checkbox {:keyfn :id}))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)
(def parser
  (om/parser {:read   read
              :mutate mutate}))

(defmethod read :graph/lines
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/selected-lines
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

#_(defmethod read :line/by-id
    [{:keys [state query]} key {:keys [id]}]
    (let [st @state]
      {:value (get-in st [:line/by-id id])}))

;;
;; "Only need to add or remove from graph/selected-lines"
;; If selected we add...
;; (pprint (get @state :graph/selected-lines))
;;
(defmethod mutate 'graph/select-line
  [{:keys [state]} _ {:keys [want-to-select? id]}]
  {:action #(let [ident [:line/by-id id]]
             (if want-to-select?
               (swap! state update :graph/selected-lines (fn [st] (-> st
                                                                      (conj ident))))
               (swap! state update :graph/selected-lines (fn [lines] (vec (remove #{ident} lines))))))})

(defmethod mutate 'app/root-refresh
  [_ _ _])

(def my-reconciler
  (om/reconciler {:normalize true                           ;; -> documentation
                  :state     init-state
                  :parser    parser}))

(defui AnyAction
  Object
  (render [this]
    (let [{:keys [text action]} (om/props this)]
      (dom/button #js{:onClick action} text))))
(def any-action (om/factory AnyAction))

(defui Root
  static om/IQuery
  (query [this]
    [{:graph/lines (om/get-query Checkbox)}
     {:graph/selected-lines (om/get-query Checkbox)}])
  Object
  (render [this]
    (println "Rendering from Root")
    (let [{:keys [graph/lines graph/selected-lines]} (om/props this)]
      (dom/div nil
               (check-default-db @my-reconciler)
               (for [line lines
                     :let [selected? (boolean (some #{line} selected-lines))]]
                 (checkbox (om/computed line {:selected? selected?})))
               (any-action {:text "Show State" :action #(pprint @my-reconciler)})
               (any-action {:text "Refresh" :action #(om/transact! this `[(app/root-refresh) :graph/lines :graph/selected-lines])})
               (dom/br nil)
               #_(any-action {:text "Add Selection" :action #(help/mutate help/norm-state true 102)})
               #_(any-action {:text "Remove Selection" :action #(help/mutate help/norm-state false 100)})
               ))))

(defn run []
  (om/add-root! my-reconciler
                Root
                (.. js/document (getElementById "main-app-area"))))

(run)

(ns misc.checkboxes
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [default-db-format.core :as db-format]
            [cljs.pprint :as pp :refer [pprint]]
            ))

(enable-console-print!)

(def init-state
  {:graph/lines
   [{:id     100
     :name "Methane"}
    {:id     101
     :name "Oxygen"}
    {:id     102
     :name "Carbon Dioxide"}
    {:id     103
     :name "Carbon Monoxide"}
    ]
   :graph/selected-lines
   [{:id     100}
    {:id     101}]
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
      (pprint check-result)
    (db-format/show-hud check-result))))

(defui Checkbox
  static om/IQuery
  (query [this]
    [:id])
  Object
  (render [this]
    (let [{:keys [id checked name]} (om/props this)]
      (dom/div #js {:className "switch demo3"}
               (dom/input #js{:type     "checkbox"
                              :checked  checked
                              :onChange (fn [e]
                                          (let [action (.. e -target -checked)]
                                            (println "Pressed so attempting to set to: " action)))})
               (dom/label nil (dom/i nil))))))
(def checkbox (om/factory Checkbox {:keyfn :id}))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)
(def parser
  (om/parser {:read read
              :mutate mutate}))

(defmethod read :graph/drop-info
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/lines
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/points
  [{:keys [state query]} key _]
  (let [st @state
        _ (println "Not called unless at root: " key ", " query)]
    {:value (om/db->tree query (get st key) st)}))

(def my-reconciler
  (om/reconciler {:normalize true ;; Documentation
                  :state     init-state
                  :parser    parser}))

(defui Root
  Object
  (render [this]
    (dom/div nil
             (check-default-db @my-reconciler)
             (checkbox {:id 1 :checked false :name "Methane"})(checkbox {:id 2 :checked true :name "Methane"}))))

(defn run []
  (om/add-root! my-reconciler
                Root
                (.. js/document (getElementById "main-app-area"))))

(run)

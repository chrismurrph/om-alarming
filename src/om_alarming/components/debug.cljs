(ns om-alarming.components.debug
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.reconciler :as reconciler]
            [om-alarming.state :as state]
            [default-db-format.core :as db-format]))

(defn add-point-to-line [state line-ident point-ident]
  (update-in state (conj line-ident :graph/points) conj point-ident))

(defn show-added-point-to-line [state line-ident point-ident]
  (get-in (add-point-to-line state line-ident point-ident) (conj line-ident :graph/points)))

(defn get-point-value [state point-ident]
  (get-in state point-ident))

(defn points-debugging [state]
  (let [point-value-fn (partial get-point-value state)]
    (dom/div nil
             (db-format/display (:graph/points state))
             (db-format/display (:graph/lines state))
             (db-format/display (get-in state [:line/by-id 100 :graph/points]))
             (db-format/display (get-in state [:line/by-id 101 :graph/points]))
             (db-format/display (get-in state [:line/by-id 102 :graph/points]))
             (db-format/display (get-in state [:line/by-id 103 :graph/points]))
             (db-format/display (map point-value-fn (get-in state [:line/by-id 103 :graph/points])))))
  ;(dom/pre nil (with-out-str (cljs.pprint/pprint (show-added-point-to-line state [:line/by-id 100] [:graph-point/by-id 2003]))))
  )

(defui Debug
  static om/IQuery
  (query [_]
    '[[:graph/receiving? _]])
  Object
  (render [this]
    (let [props (om/props this)
          ;_ (println "props:" props)
          {:keys [graph/receiving?]} props
          {:keys [state]} (om/get-computed this)]
      (dom/div nil 
               (dom/button #js {:onClick #(reconciler/alteration 'graph/toggle-receive nil :graph/receiving?)} "Receive toggle")
               (str "   Whether receiving:" receiving?)
               (dom/br nil)(dom/br nil)
               (dom/label nil (str "STATE ok?: " (db-format/ok? (db-format/check state/check-config state))))
               ;(points-debugging state)
               ;(dom/pre nil (with-out-str (cljs.pprint/pprint (get-in state [:graph/translators]))))
               (db-format/display (get-in state [:graph/translators]))
               ))))
(def debug (om/factory Debug))

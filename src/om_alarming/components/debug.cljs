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

(def by-id-fn (db-format/by-id-kw-hof "by-id"))

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

(defn mouse-debugging [state]
  (dom/div nil
           (db-format/display (:graph/hover-pos state))
           (db-format/display (:graph/last-mouse-moment state))
           (db-format/display (-> state
                                  (get-in [:plumb-line/by-id 10201 :x-position])))
           ))

(defn translators-debugging 
  [state]
  (db-format/display (get-in state [:graph/translators])))

(defn non-id-debugging
  [state]
  (db-format/display (db-format/non-by-id-entries by-id-fn state)))

(defn get-in-ids [state tuple]
  (get-in (db-format/by-id-entries by-id-fn state) tuple))

(defn some-tube [state] (get-in-ids state [:gas-at-location/by-id 512 :tube]))

(defn id-debugging
  [state]
  (db-format/display (get-in-ids state (some-tube state)))
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
               (mouse-debugging state)
               (db-format/display (:plumb-line/by-id (db-format/by-id-entries by-id-fn state)))
               ))))
(def debug (om/factory Debug))

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
           (db-format/display (get-in state (conj (get state :graph/trending-graph) :hover-pos)))
           (db-format/display (get-in state (conj (get state :graph/trending-graph) :last-mouse-moment)))
           (db-format/display (get-in state (conj (get state :graph/plumb-line) :x-position)))
           (db-format/display (get-in state (conj (get state :graph/plumb-line) :in-sticky-time?)))           
           ))

(defn translators-debugging 
  [state]
  (db-format/display (get-in state [:graph/translators])))

(defn trending-graph-debugging
  [state]
  (db-format/display (remove (fn [[k _]] (= k :graph/translators)) (get-in (db-format/table-entries by-id-fn state) [:trending-graph/by-id 10300]))))

(defn non-id-debugging
  [state]
  (db-format/display (db-format/ref-entries by-id-fn state)))

(defn get-in-ids [state tuple]
  (get-in (db-format/table-entries by-id-fn state) tuple))

(defn some-tube [state] (get-in-ids state [:gas-at-location/by-id 512 :tube]))

(defn id-debugging
  [state]
  (db-format/display (get-in-ids state (some-tube state)))
  )

(def lines-query [{:graph/lines [:id {:intersect [{:system-gas [:lowest :highest]}]}]}])

(defui Debug
  ;static om/IQuery
  ;(query [_]
  ;  [{:graph/drop-info [:graph/receiving?]}])
  Object
  (render [this]
    (let [props (om/props this)
          ;_ (println "props:" (keys props))
          ;_ (println "computed props:" (keys (om/get-computed this)))
          {:keys [receiving?]} props
          {:keys [state]} (om/get-computed this)]
      (dom/div nil 
               (dom/button #js {:onClick #(reconciler/alteration 'graph/toggle-receive nil :graph/trending-graph)} "Receive toggle")
               (str "   Receiving?" receiving?)
               (dom/br nil)(dom/br nil)
               (dom/label nil (str "STATE ok?: " (db-format/ok? (db-format/check state/check-config state))))
               ;(db-format/display (reconciler/internal-query lines-query))
               (mouse-debugging state)
               ))))
(def debug (om/factory Debug))

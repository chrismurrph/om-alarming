(ns om-alarming.parsing.mutations.nav
  (:require [untangled.client.mutations :as m]
            [untangled.dom :refer [unique-key]]))

(def tab-loadings [{:target :app/trending
                    :moves  [{:key :grid/gas-query-grid :ident [:gas-query-grid/by-id 10800]}
                             {:key :graph/trending-graph :ident [:trending-graph/by-id 10300]}]}])

(defn unload [state prev-target]
  (let [data (first (filter #(= (:target %) prev-target) tab-loadings))]
    (if data
      (let [ident [prev-target :singleton]
            dissoc-keys (mapv :key (:moves data))]
        (apply update-in state ident dissoc dissoc-keys))
      state)))

(defn load [state new-target]
  (let [data (first (filter #(= (:target %) new-target) tab-loadings))]
    (if data
      (let [ident [new-target :singleton]
            to-assoc (mapcat (fn [item] (vector (:key item) (:ident item))) (:moves data))]
        (apply update-in state ident assoc to-assoc))
      state)))

(comment (defn load-simple [state new-target]
           (case new-target
             :app/trending (-> state
                               (update-in [:app/trending :singleton] assoc
                                          :grid/gas-query-grid [:gas-query-grid/by-id 10800]
                                          :graph/trending-graph [:trending-graph/by-id 10300]
                                          ))
             state)))

(comment (defn unload-simple [state prev-target]
           (case prev-target
             :app/trending (-> state
                               (update-in [:app/trending :singleton] dissoc
                                          :grid/gas-query-grid
                                          :graph/trending-graph
                                          ))
             state)))

;;
;; Only happens once so we could check. However we should probably do this as a post-load thing. Or another idea is
;; to move current out and new in. Thus will achieve not rendering tabs that are not visible
;;
(defn change-tab [old-st new-target]
  (let [prev-target (first (get old-st :app/current-tab))
        new-state (-> old-st
                      (unload prev-target)
                      (load new-target)
                      (assoc :app/current-tab [new-target :singleton]))]
    new-state))

(defmethod m/mutate 'nav/load-tab [{:keys [state]} k {:keys [target]}]
  {:action (fn [] (swap! state change-tab target))})

(defmethod m/post-mutate :default [{:keys [state]} _ _]
  ; this won't even print:
  (println "Nothing in default for post-mutate"))

(ns om-alarming.parsing.mutations.points
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]))

(defn create-point [st x y]
  (let [id   (->> (om/db->tree [:id] (get st :graph/points) st)
                  (map :id)
                  (cons 0)
                  (reduce max)
                  inc)
        point {:id id :x x :y y}
        ref  [:graph-point/by-id id]]
    {:point ref
     :state (-> st
                (assoc-in ref point)
                (update :graph/points conj ref))}))

;;
;; Going to need to swap! but as normalized s/be easy. 
;;
;(defn add-point [st {name :name point :point}]
;  (let [{:keys [x y]} point
;        _ (println "ADD:" x y)]
;    (:state (create-point st x y))))

;(defmethod mutate 'graph/add-point
;  [{:keys [state]} _ params]
;  {:action #(swap! state add-point params)})

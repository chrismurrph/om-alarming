(ns om-alarming.parsing.mutations.points
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]))

(defn new-point
  "Modifies the state in two places - so perfectly puts in a new point.
  Caller needs to work on this state a little more to put the point in
  an existing line"
  [st x y val]
  (println "Look at" (count (get st :graph/points)))
  (let [id   (->> (om/db->tree [:id] (get st :graph/points) st)
                  (map :id)
                  (cons 0)
                  (reduce max)
                  inc)
        _ (println "In new-point, new id is " id) 
        _ (assert (> id 2000))
        point {:id id :x x :y y :val val}
        ref  [:graph-point/by-id id]]
    {:point-ident ref
     :state (-> st
                (assoc-in ref point)
                (update :graph/points conj ref))}))
(ns om-alarming.parsing.mutations.lines
  (:require [om.next :as om]
            [om-alarming.reconciler :refer [mutate]]
            [om-alarming.parsing.mutations.points :as points]
            [default-db-format.core :as db-format]
            [om-alarming.util.utils :as u]
            [om-alarming.components.log-debug :as ld]))

(defn add-to-points [points ref]
  (println "Adding: " ref " to: " points)
  (into [] (cond-> points
                   (not (some #{ref} points)) (conj ref))))

(defn add-point-to-line [state line-ident point-ident]
  (update-in state (conj line-ident :graph/points) conj point-ident))

;;
;; To create a point we need to know the name of the line that it is to go in
;; and x and y.
;;
#_(defn create-point [st params]
  (let [{:keys [line-name-ident x y val]} params
        {:keys [state point-ident]} (points/new-point st x y val)
        ]
    (-> state
        (update-in (conj line-name-ident :graph/points) conj point-ident)
        )
    ))

;;
;; params need to be line Ident, x and y
;;
#_(defmethod mutate 'graph/add-point
  [{:keys [state]} _ params]
  {:action #(swap! state create-point params)})

(defn new-line
  "Modifies the state in two places - so perfectly puts in a new line.
  Caller needs to work on this state a little more to put the line in
  an existing graph"
  [st colour intersect-id]
  (println "Look at" (count (get st :graph/lines)) " lines, new one to be " colour)
  (let [id   (->> (om/db->tree [:id] (get st :graph/lines) st)
                  (map :id)
                  (cons 99)
                  (reduce max)
                  inc)
        _ (println "In new-line, new id is " id)
        line {:id id :intersect [:gas-at-location/by-id intersect-id] :colour colour}
        ref  [:line/by-id id]]
    {:line-ident ref
     :state (-> st
                (assoc-in ref line)
                (update :graph/lines conj ref))}))

;;
;; To create a line we need to know the name of the graph that it is to go in
;; and colour and id of its intersect.
;;
(defn create-line [st params]
  (let [{:keys [graph-ident colour intersect-id]} params]
    (if colour
      (let [{:keys [state line-ident]} (new-line st colour intersect-id)]
        (-> state
            (update-in (conj graph-ident :graph/lines) conj line-ident)))
      st)))

(defn delete-line [st intersect-id]
  (let [intersect-ident [:gas-at-location/by-id intersect-id]
        line-id (:id (u/first-only (filter (fn [v] (= intersect-ident (:intersect v))) (vals (get st :line/by-id)))))
        line-ident [:line/by-id line-id]
        ]
    {:line-ident line-ident
     :state      (-> st
                     (update :graph/lines u/vec-remove-value line-ident)
                     (update :line/by-id u/unselect-keys [line-id])
                     )}))

(defn rem-line [st params]
  (let [{:keys [graph-ident intersect-id]} params
        {:keys [state line-ident]} (delete-line st intersect-id)]
    (-> state
        (update-in (conj graph-ident :graph/lines) u/vec-remove-value line-ident))))

(defmethod mutate 'graph/add-line
  [{:keys [state]} k params]
  (ld/log-mutation k)
  {:action #(swap! state create-line params)})

(defmethod mutate 'graph/remove-line
  [{:keys [state]} k params]
  (ld/log-mutation k)
  {:action #(swap! state rem-line params)})

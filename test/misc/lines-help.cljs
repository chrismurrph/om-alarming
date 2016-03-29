(ns om-alarming.misc.lines-help
  (:require [om-alarming.util.utils :as u]
            [cljs.pprint :refer [pprint]]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util.colours :as colours]))

(def norm-state (atom {:tube/gases
                                  [[:gas-at-location/by-id 500]
                                   [:gas-at-location/by-id 501]
                                   [:gas-at-location/by-id 502]
                                   [:gas-at-location/by-id 503]],
                       :customer/by-id
                                  {200 {:id 200, :first-name "Greg"},
                                   201 {:id 201, :first-name "Sally"},
                                   202 {:id 202, :first-name "Ben"}},
                       :app/tubes [[:gas-at-location/by-id 1000]],
                       :app/gases
                                  [[:gas-of-system/by-id 150]
                                   [:gas-of-system/by-id 151]
                                   [:gas-of-system/by-id 152]
                                   [:gas-of-system/by-id 153]],
                       :app/customers
                                  [[:customer/by-id 200] [:customer/by-id 201] [:customer/by-id 202]],
                       :gas-at-location/by-id
                                  {1000 {:id 1000, :tube-num 1},
                                   500
                                        {:grid-cell/id 500,
                                         :system-gas [:gas-of-system/by-id 150],
                                         :tube [:gas-at-location/by-id 1000]},
                                   501
                                        {:grid-cell/id 501,
                                         :system-gas [:gas-of-system/by-id 151],
                                         :tube [:gas-at-location/by-id 1000]},
                                   502
                                        {:grid-cell/id 502,
                                         :system-gas [:gas-of-system/by-id 152],
                                         :tube [:gas-at-location/by-id 1000]},
                                   503
                                        {:grid-cell/id 503,
                                         :system-gas [:gas-of-system/by-id 153],
                                         :tube [:gas-at-location/by-id 1000]}},
                       :line/by-id
                                  {100
                                   {:id 100,
                                    :colour {:r 255, :g 0, :b 255},
                                    :intersect [:gas-at-location/by-id 500]},
                                   101
                                   {:id 101,
                                    :colour {:r 0, :g 102, :b 0},
                                    :intersect [:gas-at-location/by-id 501]},
                                   102
                                   {:id 102,
                                    :colour {:r 0, :g 51, :b 102},
                                    :intersect [:gas-at-location/by-id 503]},
                                   103
                                   {:id 103,
                                    :colour {:r 255, :g 0, :b 0},
                                    :intersect [:gas-at-location/by-id 502]}},
                       :gas-of-system/by-id
                                  {150
                                   {:id 150,
                                    :long-name "Methane",
                                    :short-name "CH₄",
                                    :lowest 0.25,
                                    :highest 1,
                                    :units "%"},
                                   151
                                   {:id 151,
                                    :long-name "Oxygen",
                                    :short-name "O₂",
                                    :lowest 19,
                                    :highest 12,
                                    :units "%"},
                                   152
                                   {:id 152,
                                    :long-name "Carbon Monoxide",
                                    :short-name "CO",
                                    :lowest 30,
                                    :highest 55,
                                    :units "ppm"},
                                   153
                                   {:id 153,
                                    :long-name "Carbon Dioxide",
                                    :short-name "CO₂",
                                    :lowest 0.5,
                                    :highest 1.35,
                                    :units "%"}},
                       :graph/lines
                                  [[:line/by-id 100]
                                   [:line/by-id 101]
                                   [:line/by-id 102]
                                   [:line/by-id 103]]}))

(defui AnyAction
       Object
       (render [this]
               (let [{:keys [text action]} (om/props this)]
                 (dom/button #js{:onClick action} text))))
(def any-action (om/factory AnyAction))

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
    (if graph-ident
      (-> state
          (update-in (conj graph-ident :graph/lines) u/vec-remove-value line-ident))
      state)))

;;
;; To create a line we need to know the name of the graph that it is to go in
;; and colour and id of its intersect.
;;
(defn create-line [st params]
  (let [{:keys [graph-ident colour intersect-id]} params]
    (if colour
      (let [{:keys [state line-ident]} (new-line st colour intersect-id)]
        (if graph-ident
          (-> state
              (update-in (conj graph-ident :graph/lines) conj line-ident))
          state))
      st)))

(defn mutate [state want-to-select? id]
  (if want-to-select?
    (swap! state create-line {:intersect-id id :colour (colours/new-random-colour)})
    (swap! state rem-line {:intersect-id id}))
  (pprint @state))

(ns om-alarming.test-mutations
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pp :refer [pprint]]
            [om-alarming.util.utils :as u]
            [om-alarming.mutations-state :as st]))

(comment
  "For testing mutations where they won't silently fail on you. Intention is to make it a bit better
  by having button/s textarea/s from the Root component. Pressing buttons clears the UI. Even probe
  results can come out the the UI. Just more comfortable than using the Chrome dev console")

(enable-console-print!)

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)
(def parser
  (om/parser {:read read
              :mutate mutate}))

(def reconciler
  (om/reconciler {:normalize false
                  :state st/state
                  :parser parser}))

(defui Root
       ;static om/IQuery
       ;(query [this]
       ;       [:id])
       Object
       (render [this]
         ;(show-db)
         )
       )

(defn delete-line [st intersect-id]
  (let [intersect-ident [:gas-at-location/by-id intersect-id]
        line-id (:id (u/first-only (filter (fn [v] (= intersect-ident (:intersect v))) (vals (get st :line/by-id)))))
        line-ident [:line/by-id line-id]
        _ (println "In delete-line, line-id,line-ident is " line-id line-ident)
        ]
    {:line-ident line-ident
     :state      (-> st
                     (update :graph/lines u/vec-remove-value line-ident)
                     (update :line/by-id u/unselect-keys [line-id])
                     )}))

(def rm-line-params {:graph-ident [:trending-graph/by-id 10300] :intersect-id 501})
(defn rem-line [st params]
  (let [{:keys [graph-ident intersect-id]} params
        {:keys [state line-ident]} (delete-line st intersect-id)
        ]
    (-> state
        (update-in (conj graph-ident :graph/lines) u/vec-remove-value line-ident)
        )
    ))

;;
;; Will have to reduce over every line ident with state being the accumulator
;;
(defn rm-all-points-from-line [st line-ident]
  (-> st
      (assoc-in (conj line-ident :graph/points) [])))

(defn rm-all-points [st]
  (let [all-lines-idents (:graph/lines st)]
    (-> (reduce rm-all-points-from-line
                st
                all-lines-idents)
        (assoc :graph/points []))))

(defn run []
  (om/add-root! reconciler
                Root
                (.. js/document (getElementById "main-app-area")))
  ;(pprint (:graph/points @st/state))
  ;(pprint (:graph/lines @st/state))
  ;(pprint (get-in @st/state [:line/by-id 100]))
  ;(pprint (assoc (get-in @st/state [:line/by-id 100]) :graph/points []))
  ;;
  ;(pprint (rm-all-points @st/state))
  ;(pprint (:graph/lines (rm-all-points @st/state)))
  ;(pprint (:line/by-id (rm-all-points-from-line @st/state [:line/by-id 100])))
  (pprint (:line/by-id (rm-all-points @st/state)))
  (pprint (:graph/points (rm-all-points @st/state)))
  )

(run)

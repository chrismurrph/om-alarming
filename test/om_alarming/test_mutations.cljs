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
                     (update :graph/lines u/remove-value line-ident)
                     (update :line/by-id u/unselect-keys [line-id])
                     )}))

(defn rem-line [st params]
  (let [{:keys [graph-ident intersect-id]} params
        {:keys [state line-ident]} (delete-line st intersect-id)
        ]
    (-> state
        (update-in (conj graph-ident :graph/lines) u/remove-value line-ident)
        )
    ))

(def rm-params {:graph-ident [:trending-graph/by-id 10300] :intersect-id 501})

(defn run []
  (om/add-root! reconciler
                Root
                (.. js/document (getElementById "main-app-area")))
  (pprint (:graph/lines @reconciler))
  (pprint (:line/by-id @reconciler))

  (pprint (:graph/lines (swap! st/state rem-line rm-params)))
  )

(run)

(ns om-alarming.core
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.reconciler :as reconciler :refer [my-reconciler]]
            [om-alarming.parsing.reads]
            [om-alarming.util.utils :as u]
            [om-alarming.components.grid :as grid]
            [om-alarming.components.debug :as debug]
            [om-alarming.components.general :as gen]
            [om-alarming.components.nav :as nav]
            [om-alarming.components.graphing :as graph]
            [om-alarming.graph.processing :as p]
            [cljs.pprint :as pp :refer [pprint]]
            [default-db-format.core :as db-format]
            [om-alarming.graph.incoming :as in]
            [cljs-time.core :as t]
            [om-alarming.graph.staging-area :as sa]
            [om-alarming.graph.mock-values :as db]
            [cljs.core.async :as async :refer [<!]]
            [om-alarming.parsing.mutations.lines]
            [om-alarming.parsing.mutations.graph]
            [om-alarming.state :as state])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

;;
;; When get rid of this also remove :receiving-chan from the state
;;
(defn halt-receiving-wrong
  "Grabs the receiving chan from state and sends it {:pause true}"
  []
  (let [chan (reconciler/internal-query [{:graph/trending-graph {:graph/misc [:receiving-chan]}}])
        _ (println "PAUSE: " chan)]))

(defn halt-receiving []
  (reconciler/alteration 'graph/stop-receive nil :receiving?))

(defn check-default-db [st]
  (let [version db-format/version
        check-result (db-format/check state/check-config st)
        ok? (db-format/ok? check-result)
        msg-boiler (str "normalized (default-db-format ver: " version ")")
        message (if ok?
                  (str "GOOD: state fully " msg-boiler)
                  (str "BAD: state not fully " msg-boiler))]
    (println message)
    (when (not ok?)
      ;(pprint check-result)
      (pprint st)
      (halt-receiving))
    (db-format/show-hud check-result)))

(defui App
  static om/IQuery
  (query [this]
    [
     {:app/gases (om/get-query gen/SystemGas)}
     {:app/tubes (om/get-query grid/GridRow)}
     {:tube/gases (om/get-query grid/GridDataCell)}
     {:app/buttons (om/get-query nav/TabButton)}
     {:app/selected-button (om/get-query nav/TabButton)}
     {:graph/points (om/get-query graph/Point)}
     {:graph/x-gas-details (om/get-query graph/RectTextTick)}
     {:graph/labels (om/get-query graph/Label)}
     {:graph/trending-graph (om/get-query graph/TrendingGraph)}
     {:graph/lines (om/get-query graph/Line)}
     {:graph/drop-info (om/get-query graph/DropInfo)}
     {:graph/plumb-line (om/get-query graph/PlumbLine)}
     {:graph/misc (om/get-query graph/Misc)}
     {:graph/x-gas-details (om/get-query graph/RectTextTick)}
     ])
  Object
  (render [this]
    (let [app-props (om/props this)]
      (dom/div nil
               (check-default-db @my-reconciler)
               (nav/menu-bar (:app/buttons app-props)
                             (:app/selected-button app-props))
               (let [selected (:name (:app/selected-button app-props))]
                 (case selected
                   "Map" (dom/div nil "Nufin")
                   "Trending" (grid/gas-query-panel app-props)
                   "Thresholds" (dom/div nil "Nufin")
                   "Reports" (dom/div nil "Nufin")
                   "Automatic" (dom/div nil "Nufin")
                   "Logs" (dom/div nil "Nufin")
                   "Debug" (debug/debug (om/computed (:graph/trending-graph app-props) {:state @my-reconciler}))
                   nil (dom/div nil "Nothing selected, program has crashed!")
                   ))))))

(defn ident-finder
  "Hack b/c we will stop using names...
  Given a name will return an Ident"
  [name-id-maps idents]
  (fn [name]
    (let [id (:id (first (filter #(= (-> % :name) name) name-id-maps)))
          ;_ (println id)
          ident (first (filter #(= (-> % second) id) idents))]
      ident)))

(def lines-query [{:graph/lines [:id {:intersect [{:system-gas [:lowest :highest :long-name]}]}]}])

(defn to-info [line-query-res]
  (let [system-gas (-> line-query-res :intersect :system-gas)]
    {:ref [:line/by-id (:id line-query-res)]
     :lowest (-> system-gas :lowest)
     :highest (-> system-gas :highest)
     :name (-> system-gas :long-name)}))

(defn ^:export run []
  (om/add-root! my-reconciler
                App
                (.. js/document (getElementById "main-app-area")))
  (p/init)
  (let [line-q-results (:graph/lines (reconciler/internal-query lines-query))
        now (t/now)
        now-millis (.getTime now)
        week-ago-millis (.getTime (t/minus now (t/weeks 1)))
        line-infos (map to-info line-q-results)
        _ (println "line-infos: " line-infos)
        chan (in/query-remote-server line-infos week-ago-millis now-millis)
        receiving-chan (sa/show line-infos week-ago-millis now-millis chan)
        _ (reconciler/alteration 'graph/receiving-chan {:receiving-chan receiving-chan} :graph/misc)
        ]
    (go-loop [count 0]
             (let [{:keys [info point]} (<! receiving-chan)
                   paused? (not (:receiving? (:graph/trending-graph (reconciler/internal-query [{:graph/trending-graph [:receiving?]}]))))
                   x (first point)
                   y (second point)
                   val (last point)
                   line-ident (:ref info)
                   ;_ (println "Ident: " line-ident)
                   _ (assert line-ident)
                   ]
               (if (and (< count 20) (not paused?))
                 (do
                   (reconciler/alteration 'graph/add-point
                                          {:line-name-ident line-ident :x x :y y :val val}
                                          :graph/lines)
                   (println "Receiving " name x y)
                   (recur (inc count)))
                 (recur count))))))
(run)

;ident (line-name->ident name)]
;;(println "Receiving " name x y)

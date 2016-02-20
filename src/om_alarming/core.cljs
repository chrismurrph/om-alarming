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
  (let [chan (reconciler/internal-query [{:graph/misc [:receiving-chan]}])]
    (println "PAUSE: " chan)))

(defn halt-receiving []
  (reconciler/alteration 'graph/stop-receive nil :graph/receiving?))

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
      (pprint check-result)
      ;(pprint state)
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
     {:trending (om/get-query graph/TrendingGraph)}
     ;; Have to do?:
     {:graph/init [:width :height]}
     ;; Had to do otherwise not filled up below
     {:graph/lines (om/get-query graph/Line)}
     {:graph/drop-info (om/get-query graph/DropInfo)}
     {:graph/plumb-line (om/get-query graph/PlumbLine)}
     {:debug (om/get-query debug/Debug)}
     ;; Not sure, try to remove later:
     {:graph/misc [:comms :receiving-chan]}
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
                   "Debug" (debug/debug (om/computed (:debug app-props) {:state @my-reconciler}))
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

(defn ^:export run []
  (om/add-root! my-reconciler
                App
                (.. js/document (getElementById "main-app-area")))
  (p/init)
  (let [line-names (keys db/my-lines)
        ;'("Methane at 1", "Oxygen at 4", "Carbon Dioxide at 2", "Carbon Monoxide at 3")
        _ (println "NAMES: " line-names)
        ;; Not only are we going to get rid of names, but they might change all the time
        line-idents (reconciler/internal-query [:graph/line-idents])
        data-values (reconciler/internal-query [{:graph/lines [:id :name]}])
        finder (ident-finder (:graph/lines data-values) (:graph/line-idents line-idents))
        line-name->ident (into {} (map (fn [x y] [x y]) line-names (map finder line-names)))
        ;_ (println line-name->ident)
        now (t/now)
        now-millis (.getTime now)
        week-ago-millis (.getTime (t/minus now (t/weeks 1)))
        chan (in/query-remote-server line-names week-ago-millis now-millis)
        ; The lines are already in state so no need for this
        ;_ (sa/create @db/lines)
        receiving-chan (sa/show @db/lines week-ago-millis now-millis chan)
        _ (reconciler/alteration 'graph/receiving-chan {:receiving-chan receiving-chan} :graph/misc)
        ]
    (go-loop [count 0]
             (let [{:keys [name point]} (<! receiving-chan)
                   paused? (not (reconciler/top-level-query :graph/receiving?))
                   x (first point)
                   y (second point)
                   val (last point)
                   line-ident (line-name->ident name)
                   ;_ (println "Ident: " line-ident)
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

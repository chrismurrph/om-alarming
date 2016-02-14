(ns om-alarming.core
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.reconciler :refer [my-reconciler]]
            [om-alarming.parsing.reads]
            [om-alarming.util.utils :as u]
            [om-alarming.components.grid :as grid]
            [om-alarming.components.general :as gen]
            [om-alarming.components.nav :as nav]
            [om-alarming.components.graphing :as graph]
            [om-alarming.graph.processing :as p]
            [cljs.pprint :as pp :refer [pprint]]
            [default-db-format.core :as db-format]
            [om-alarming.reconciler :as reconciler]
            [om-alarming.graph.incoming :as in]
            [cljs-time.core :as t]
            [om-alarming.graph.staging-area :as sa]
            [om-alarming.graph.mock-values :as db]
            [cljs.core.async :as async :refer [<!]]
            [om-alarming.parsing.mutations.lines])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

(def irrelevant-keys #{:graph/labels-visible?
                       :graph/hover-pos
                       :graph/misc
                       :graph/translators
                       :graph/init
                       :graph/last-mouse-moment
                       :om.next/queries
                       })
(def okay-val-maps #{[:r :g :b]})
(def check-config {:excluded-keys irrelevant-keys
                   :okay-value-maps okay-val-maps
                   :by-id-kw "by-id"})

(defn halt-receiving
  "Grabs the receiving chan from state and sends it {:pause true}"
  []
  (let [chan (reconciler/internal-query [{:graph/misc [:receiving-chan]}])]
    (println "PAUSE: " chan)))

(defn check-default-db? [state]
  (let [version db-format/version
        check-result (db-format/check check-config state)
        ok? (db-format/ok? check-result)
        msg-boiler (str "normalized (default-db-format ver: " version ")")
        message (if ok?
                  (str "GOOD: state fully " msg-boiler)
                  (str "BAD: state not fully " msg-boiler))
        ]
    (db-format/display check-result)
    (println message)
    (when (not ok?)
      ;(pprint check-result)
      (pprint state)
      (halt-receiving)
      )
    ok?))

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
     ;{:graph/init [:width :height]}
     ;; Had to do otherwise not filled up below
     {:graph/lines (om/get-query graph/Line)}
     {:graph/drop-info (om/get-query graph/DropInfo)}
     {:graph/plumb-line (om/get-query graph/PlumbLine)}
     ;; Not sure, try to remove later:
     {:graph/misc [:comms :receiving-chan]}
     ])
  Object
  (render [this]
    (let [app-props (om/props this)]
      (dom/div nil
               (check-default-db? @my-reconciler)
               (nav/menu-bar (:app/buttons app-props)
                             (:app/selected-button app-props))
               (let [selected (:name (:app/selected-button app-props))
                     _ (assert selected)]
                 (case selected
                   "Map" (dom/div nil "Nufin")
                   "Trending" (grid/gas-query-panel app-props)
                   "Thresholds" (dom/div nil "Nufin")
                   "Reports" (dom/div nil "Nufin")
                   "Automatic" (dom/div nil "Nufin")
                   "Logs" (dom/div nil "Nufin")
                   ))))))

(defn run []
  (om/add-root! my-reconciler
                App
                (.. js/document (getElementById "main-app-area")))
  (p/init)
  (let [line-names (keys db/my-lines)
        ;'("Methane at 1", "Oxygen at 4", "Carbon Dioxide at 2", "Carbon Monoxide at 3")
        _ (println "NAMES: " line-names)
        line-idents (reconciler/internal-query [:graph/line-idents])
        _ (println "line idents: " line-idents)
        first-ident (-> line-idents :graph/line-idents first)
        _ (println first-ident)
        data (reconciler/internal-query [{:graph/lines [:id :name]}])
        _ (println "data:" data)
        now (t/now)
        now-millis (.getTime now)
        week-ago-millis (.getTime (t/minus now (t/weeks 1)))
        chan (in/query-remote-server line-names week-ago-millis now-millis)
        ; The lines are already in state so no need for this
        ;_ (sa/create @db/lines)
        receiving-chan (sa/show @db/lines week-ago-millis now-millis chan)
        _ (reconciler/alteration 'graph/receiving-chan {:receiving-chan receiving-chan} :graph/misc)
        ]
    (go-loop []
             (let [{:keys [name point pause?]} (<! receiving-chan)
                   x (first point)
                   y (second point)]
               (println "Receiving " name x y)
               (when (not pause?)
                 (reconciler/alteration 'graph/add-point
                                        {:name name :point {:x x :y y}})))
             (recur)))
  )
(run)

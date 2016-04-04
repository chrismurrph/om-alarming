(ns om-alarming.core
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.reconciler :as reconciler :refer [my-reconciler]]
            [om-alarming.parsing.reads]
            [om-alarming.util.utils :as u]
            [om-alarming.util.colours :as colours]
            [om-alarming.components.grid :as grid]
            [om-alarming.components.debug :as debug]
            [om-alarming.components.general :as gen]
            [om-alarming.components.nav :as nav]
            [om-alarming.components.graphing :as graph]
            [om-alarming.components.navigator :as navigator]
            [om-alarming.components.d3 :as d3]
            [om-alarming.components.no-d3-just-svg :as no-d3]
            [om-alarming.components.log-debug :as ld]
            [om-alarming.components.login-dialog :as dialog]
            [om-alarming.graph.processing :as p]
            [cljs.pprint :as pp :refer [pprint]]
            [default-db-format.core :as db-format]
            [om-alarming.graph.incoming :as in]
            [om-alarming.util.colours :as colours]
            [cljs-time.core :as time]
            [om-alarming.graph.staging-area :as sa]
            [om-alarming.graph.mock-values :as db]
            [cljs.core.async :as async :refer [<!]]
            [om-alarming.parsing.mutations.lines]
            [om-alarming.parsing.mutations.graph]
            [om-alarming.state :as state]
            [devtools.core :as devtools])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

;(defn halt-receiving []
;  (reconciler/alteration 'graph/stop-receive nil :receiving?))

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
      (db-format/show-hud check-result))))

(defui Map
  ;static om/Ident
  ;(ident [this props]
  ;  [:map/by-id (:id props)])  
  static om/IQuery
  (query [this]
    [:id :map/name :map/description])
  Object
  (render [this]
    (ld/log-render "Map" this)
    (dom/div nil "Nufin")))

(defui Thresholds
  static om/IQuery
  (query [this]
    [:id :thresholds/name :thresholds/description])
  Object
  (render [this]
    (ld/log-render "Thresholds" this)
    (dom/div nil "Nufin")))

(defui Reports
  static om/IQuery
  (query [this]
    [:id :reports/name :reports/description])
  Object
  (render [this]
    (ld/log-render "Reports" this)
    (dom/div nil "Nufin")))

(defui Automatic
  static om/IQuery
  (query [this]
    [:id :automatic/name :automatic/description])
  Object
  (render [this]
    (ld/log-render "Automatic" this)
    (dom/div nil "Nufin")))

(defui Logs
  static om/IQuery
  (query [this]
    [:id :logs/name :logs/description])
  Object
  (render [this]
    (ld/log-render "Logs" this)
    (dom/div nil "Nufin")))

;; These wrong now
(def route->component
  {
   :app/map Map
   :app/trending graph/TrendingGraph
   :app/thresholds Thresholds
   :app/reports Reports
   :app/automatic Automatic
   :app/logs Logs
   :app/debug debug/Debug
   }
  )

#_(def route->factory
  (zipmap (keys route->component)
          (map (fn [c] (om/factory c {:keyfn :id})) (vals route->component))))

(defn props->route [props]
  (let [;_ (println "PROPs: " props)
        res (-> props :app/route first)]
    res))

(defui App
  static om/IQuery
  (query [this]
    (let [
          ;subq-ref (if (om/component? this)
          ;           (props->route (om/props this))
          ;           :app/map)
          ;_ (assert subq-ref)
          ;subq-class (get route->component subq-ref)
          ;_ (assert subq-class (str "Can't find using: <" subq-ref ">"))
          ;_ (println "IN:" subq-ref subq-class)
          ]
      [
       ;:app/route {:route/data (om/subquery this subq-ref subq-class)}
       {:app/login-info (om/get-query dialog/LoginDialog)}
       {:app/sys-gases (om/get-query gen/SystemGas)}
       {:app/tubes (om/get-query gen/Location)}
       {:tube/real-gases (om/get-query grid/GridDataCell)}
       {:app/buttons (om/get-query nav/TabButton)}
       {:app/selected-button (om/get-query nav/TabButton)}
       {:graph/x-gas-details (om/get-query graph/RectTextTick)}
       {:graph/labels (om/get-query graph/Label)}
       {:graph/trending-graph (om/get-query graph/TrendingGraph)}
       {:graph/navigator (om/get-query navigator/GraphNavigator)}
       {:grid/gas-query-grid (om/get-query grid/GasQueryGrid)}
       {:grid/gas-query-panel (om/get-query grid/GasQueryPanel)}
       {:graph/lines (om/get-query graph/Line)}
       {:graph/plumb-line (om/get-query graph/PlumbLine)}
       {:graph/misc (om/get-query graph/Misc)}
       [:debug/squares '_]
       ]))
  Object
  (pick-colour [this cols]
    (colours/new-random-colour cols))
  (click-cb [this existing-colours id selected?]
    (let [pick-colour-fn #(.pick-colour this existing-colours)]
      (if selected?
        (om/transact! this `[(graph/remove-line {:graph-ident [:trending-graph/by-id 10300] :intersect-id ~id})])
        (om/transact! this `[(graph/add-line {:graph-ident [:trending-graph/by-id 10300] :intersect-id ~id :colour ~(pick-colour-fn)})]))))
  (cancel-sign-in-fn [this]
    (println "user cancelled, doing nothing, we ought to take user back to web page came from"))
  (sign-in-fn [this]
    (om/transact! this `[(app/authenticate)]))
  (general-update [this ident data]
    (om/transact! this `[(app/update {:ident ~ident :data ~data})]))
  (render [this]
    (ld/log-render "App" this)
    (let [app-props (om/props this)
          {:keys [app/route route/data app/login-info app/buttons app/selected-button graph/lines grid/gas-query-panel]} app-props
          existing-colours (into #{} (map :colour lines))]
      (dom/div nil
               (check-default-db @my-reconciler)
               (if (not (:app/authenticated? login-info))
                 (dialog/login-dialog (om/computed login-info {:sign-in-fn #(.sign-in-fn this)
                                                               :cancel-sign-in-fn #(.cancel-sign-in-fn this)
                                                               :update-fn #(.general-update this %1 %2)}))
                 (dom/div nil
                          (nav/menu-bar buttons
                                        selected-button)
                          (let [selected (:name selected-button)]
                            (case selected
                              "Map" (dom/div nil "Nufin")
                              "Trending" (grid/gas-query-panel-component (om/computed gas-query-panel {:lines lines :click-cb-fn #(.click-cb this existing-colours %1 %2)}))
                              "New Trending" (d3/present-defcard)
                              "SVG Trending" (no-d3/present-defcard)
                              "Thresholds" (dom/div nil "Nufin")
                              "Reports" (dom/div nil "Nufin")
                              "Automatic" (dom/div nil "Nufin")
                              "Logs" (dom/div nil "Nufin")
                              "Debug" (debug/debug (om/computed (merge (u/probe "NAV" (:graph/navigator app-props))
                                                                       (:graph/trending-graph app-props))
                                                                {:state @my-reconciler}))
                              nil (dom/div nil "Nothing selected, program has crashed!")))))))))

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
  (devtools/enable-feature! :sanity-hints :dirac)
  (devtools/install!)
  (p/init))
(run)

;ident (line-name->ident name)]
;;(println "Receiving " name x y)

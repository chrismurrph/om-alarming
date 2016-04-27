(ns om-alarming.root
  (:require om-alarming.parsing.mutations.app
            om-alarming.parsing.mutations.graph
            om-alarming.parsing.mutations.lines
            om-alarming.parsing.mutations.nav
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [default-db-format.core :as db-format]
            [om-alarming.new-core :refer [my-reconciler app]]
            [om-alarming.query :as q]
            [cljs.pprint :as pp :refer [pprint]]
            [om-alarming.ui :as ui]
            [om-alarming.components.general :as gen]
            [om-alarming.components.graphing :as graph]
            [om-alarming.components.grid :as grid]
            [om-alarming.state :as state]
            [om-alarming.util.colours :as colours]
            [om-alarming.parsing.mutations.lines]
            [om-alarming.graph.processing :as p]
            [om-alarming.sente-client :as client]
            [untangled.client.core :as uc]))

(defn tab-style [tab kw]
  #js{:className (str "pure-menu-item" (if (= tab kw) " pure-menu-selected"))})

(defn check-default-db [st]
  (let [_ (assert (not (nil? st)))
        version db-format/version
        check-result (db-format/check state/check-config st)
        ok? (db-format/ok? check-result)
        msg-boiler (str "normalized (default-db-format ver: " version ")")
        message (if ok?
                  (str "GOOD: state fully " msg-boiler)
                  (str "BAD: state not fully " msg-boiler))]
    (println message)
    (when (not ok?)
      (do
        (pprint check-result)
        (pprint st))
      (db-format/show-hud check-result))))

(defui ^:once App
  static om/IQuery
  (query [this] (into q/non-union-part-of-root-query
                      [;:ui/locale
                       ;:ui/react-key
                       {:app/current-tab (om/get-query ui/TabUnion)}
                       ]))
  Object
  (pick-colour [this cols]
    (colours/new-random-colour cols))
  (click-cb [this existing-colours id selected?]
    (let [pick-colour-fn #(.pick-colour this existing-colours)]
      (if selected?
        (om/transact! this `[(graph/remove-line {:graph-ident [:trending-graph/by-id 10300] :intersect-id ~id})])
        (om/transact! this `[(graph/add-line {:graph-ident [:trending-graph/by-id 10300] :intersect-id ~id :colour ~(pick-colour-fn)})]))))
  (render [this]
    (let [{:keys [app/current-tab graph/lines ui/react-key] :or {ui/react-key "ROOT"} :as props} (om/props this)
          {:keys [tab/type tab/label]} current-tab
          _ (println "tab is " type "")
          ;my-reconciler (:reconciler @core/app)
          existing-colours (into #{} (map :colour lines))
          ]
      (dom/div nil
               (if my-reconciler
                 (check-default-db @my-reconciler)
                 (println "reconciler not available in Root component when first mounted"))
               (dom/div #js{:className "custom-wrapper pure-g"
                            :id        "menu"}
                        (dom/div #js{:className "pure-u-1 pure-u-md-1-3"}
                                 (dom/div #js{:className "pure-menu"}
                                          (dom/a #js{:className "pure-menu-heading custom-brand"
                                                     :href      "#"} "Mystery App!")
                                          (dom/a #js{:className "custom-toggle"
                                                     :id        "toggle"}
                                                 (dom/s #js{:className "bar"})
                                                 (dom/s #js{:className "bar"}))))
                        (dom/div #js{:className "pure-u-1 pure-u-md-1-3"}
                                 (dom/div #js{:className "pure-menu pure-menu-horizontal custom-can-transform"}
                                          (dom/ul #js{:className "pure-menu-list"}
                                                  (dom/li (tab-style type :app/map)
                                                          (dom/a #js{:className "pure-menu-link"
                                                                     :href      "#"
                                                                     :onClick   #(om/transact! this '[(nav/load-tab {:target :app/map})])} "Map"))
                                                  (dom/li (tab-style type :app/trending)
                                                          (dom/a #js{:className "pure-menu-link"
                                                                     :href      "#"
                                                                     :onClick   #(om/transact! this '[(nav/load-tab {:target :app/trending})])} "Trending"))
                                                  (dom/li (tab-style type :app/thresholds)
                                                          (dom/a #js{:className "pure-menu-link"
                                                                     :href      "#"
                                                                     :onClick   #(om/transact! this '[(nav/load-tab {:target :app/thresholds})])} "Thresholds"))
                                                  (dom/li (tab-style type :app/reports)
                                                          (dom/a #js{:className "pure-menu-link"
                                                                     :href      "#"
                                                                     :onClick   #(om/transact! this '[(nav/load-tab {:target :app/reports})])} "Reports")))))
                        (dom/div #js{:className "pure-u-1 pure-u-md-1-3"}
                                 (dom/div #js{:className "pure-menu pure-menu-horizontal custom-menu-3 custom-can-transform"}
                                          (dom/ul #js{:className "pure-menu-list"}
                                                  (dom/li #js{:className "pure-menu-item"}
                                                          (dom/a #js{:className "pure-menu-link"
                                                                     :href      "#"
                                                                     :onClick #(pprint @my-reconciler)} "Help"))))))
               (dom/div nil
                        (ui/ui-tab (om/computed current-tab {:click-cb-fn #(.click-cb this existing-colours %1 %2)})))))))

(reset! app (uc/mount @app App "main-app-area"))

;(defn ^:export run []
;  (om/add-root! my-reconciler
;                App
;                (.. js/document (getElementById "main-app-area")))
;  (p/init)
;  (client/start!))
;(run)

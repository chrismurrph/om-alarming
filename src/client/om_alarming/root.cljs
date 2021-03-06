(ns om-alarming.root
  (:require om-alarming.parsing.mutations.app
            om-alarming.parsing.mutations.graph
            om-alarming.parsing.mutations.lines
            om-alarming.parsing.mutations.nav
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [default-db-format.core :as db-format]
            [om-alarming.core :as core]
            [om-alarming.query :as q]
            [cljs.pprint :as pp :refer [pprint]]
            [om-alarming.ui :as ui]
            [om-alarming.state :as state]
            [om-alarming.util.colours :as colours]
            [om-alarming.parsing.mutations.lines]
            [untangled.client.core :as uc]
            [om-alarming.components.login-dialog :as dialog]
            [om-alarming.sente-client :as client]))

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

(defn change-tab-hof [component which-tab-kw]
  (fn []
    (let [props (om/props component)
          ;_ (println "PROPS: " (:app/login-info props))
          ]
      (when (not (-> props :app/login-info :app/server-state-loaded?))
        (client/chsk-send!
          [:app/startup-info]
          5000
          (fn [cb-reply]
            (println "TZ Info: " cb-reply)
            (let [data (:some-reply cb-reply)]
              (om/transact! component `[(app/server-info ~data)]))))))
    (om/transact! component `[(nav/load-tab {:target ~which-tab-kw})])))

(defui ^:once App
  static om/IQuery
  (query [this] (into q/non-union-part-of-root-query
                      [;:ui/locale
                       ;:ui/react-key
                       {:app/current-tab (om/get-query ui/TabUnion)}
                       ]))
  Object
  (initLocalState [this]
    {:last-time-auth? false})
  (pick-colour [this cols]
    (colours/new-random-colour cols))
  (click-for-line-cb [this existing-colours cell id selected?]
    (let [pick-colour-fn #(.pick-colour this existing-colours)]
      (if selected?                                         ;; graph/remove-line and graph/add-line s/not need ident in params or follow-on read!!
        (om/transact! cell `[(graph/remove-line {:intersect-id ~id}) [:trending-graph/by-id 10300]])
        (om/transact! cell `[(graph/add-line {:intersect-id ~id :colour ~(pick-colour-fn)}) [:trending-graph/by-id 10300]]))))
  (cancel-sign-in-fn [this]
    (println "user cancelled, doing nothing, we ought to take user back to web page came from"))
  (sign-in-fn [this un pw]
    (println "Trying to sign in for: " un pw)
    (client/login-process un pw))
  (general-update [this ident data]
    (om/transact! this `[(app/update {:ident ~ident :data ~data})]))
  (render [this]
    (let [{:keys [app/current-tab app/login-info graph/lines ui/react-key app/server-info] :or {ui/react-key "ROOT"} :as props} (om/props this)
          {:keys [tab/type tab/label]} current-tab
          {:keys [app/authenticated?]} login-info
          ;_ (println "tab is " type "")
          existing-colours (into #{} (map :colour lines))
          ]
      (dom/div nil
               (if (core/my-reconciler-available?)
                 (check-default-db @(core/my-reconciler))
                 (println "reconciler not available in Root component when first mounted"))
               (if (not authenticated?)
                 (dialog/login-dialog (om/computed login-info {:sign-in-fn        #(.sign-in-fn this %1 %2)
                                                               :cancel-sign-in-fn #(.cancel-sign-in-fn this)
                                                               :update-fn         #(.general-update this %1 %2)}))
                 (dom/div nil
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
                                                                                :onClick   (change-tab-hof this :app/map)} "Map"))
                                                             (dom/li (tab-style type :app/trending)
                                                                     (dom/a #js{:className "pure-menu-link"
                                                                                :href      "#"
                                                                                :onClick   (change-tab-hof this :app/trending)} "Trending"))
                                                             (dom/li (tab-style type :app/thresholds)
                                                                     (dom/a #js{:className "pure-menu-link"
                                                                                :href      "#"
                                                                                :onClick   (change-tab-hof this :app/thresholds)} "Thresholds"))
                                                             (dom/li (tab-style type :app/reports)
                                                                     (dom/a #js{:className "pure-menu-link"
                                                                                :href      "#"
                                                                                :onClick   (change-tab-hof this :app/reports)} "Reports"))
                                                             (dom/li (tab-style type :app/sente)
                                                                     (dom/a #js{:className "pure-menu-link"
                                                                                :href      "#"
                                                                                :onClick   (change-tab-hof this :app/sente)} "Sente")))))
                                   (dom/div #js{:className "pure-u-1 pure-u-md-1-3"}
                                            (dom/div #js{:className "pure-menu pure-menu-horizontal custom-menu-3 custom-can-transform"}
                                                     (dom/ul #js{:className "pure-menu-list"}
                                                             (dom/li #js{:className "pure-menu-item"}
                                                                     (dom/a #js{:className "pure-menu-link"
                                                                                :href      "#"
                                                                                :onClick   #(pprint @(core/my-reconciler))} "Help"))))))
                          (ui/ui-tab (om/computed current-tab {:click-cb-fn #(.click-for-line-cb this existing-colours %1 %2 %3)}))))))))

(reset! core/app (uc/mount @core/app App "main-app-area"))
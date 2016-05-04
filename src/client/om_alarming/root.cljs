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

(comment
  )

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
  (click-cb [this existing-colours cell id selected?]
    (let [pick-colour-fn #(.pick-colour this existing-colours)]
      (if selected?                                         ;; graph/remove-line and graph/add-line s/not need ident in params or follow-on read!!
        (om/transact! cell `[(graph/remove-line {:graph-ident [:trending-graph/by-id 10300] :intersect-id ~id}) :graph/trending-graph #_[:trending-graph/by-id 10300]])
        (om/transact! cell `[(graph/add-line {:graph-ident [:trending-graph/by-id 10300] :intersect-id ~id :colour ~(pick-colour-fn)}) :graph/trending-graph #_[:trending-graph/by-id 10300]]))))
  (cancel-sign-in-fn [this]
    (println "user cancelled, doing nothing, we ought to take user back to web page came from"))
  (sign-in-fn [this un pw]
    (println "Trying to sign in for: " un pw)
    (client/login-process un pw))
  (general-update [this ident data]
    (om/transact! this `[(app/update {:ident ~ident :data ~data})]))
  (render [this]
    (let [{:keys [app/current-tab app/login-info graph/lines ui/react-key] :or {ui/react-key "ROOT"} :as props} (om/props this)
          {:keys [tab/type tab/label]} current-tab
          {:keys [app/authenticated?]} login-info
          previously-authenticated? (:last-time-auth? (om/get-state this))
          _ (om/set-state! this {:last-time-auth? authenticated?})
          ;_ (println "tab is " type "")
          existing-colours (into #{} (map :colour lines))
          ;_ (println (str "NOW: " authenticated? " BEFORE: " previously-authenticated?))
          just-logged-in? (and authenticated? (not previously-authenticated?))
          _ (when just-logged-in?
              (js/setTimeout
                (fn [] (client/chsk-send!
                         [:app/startup-info {}] (fn [cb-reply]
                                                  (println "TZ Info: " cb-reply))))
                2000))
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
                                                                                :onClick   #(om/transact! this '[(nav/load-tab {:target :app/reports})])} "Reports"))
                                                             (dom/li (tab-style type :app/sente)
                                                                     (dom/a #js{:className "pure-menu-link"
                                                                                :href      "#"
                                                                                :onClick   #(om/transact! this '[(nav/load-tab {:target :app/sente})])} "Sente")))))
                                   (dom/div #js{:className "pure-u-1 pure-u-md-1-3"}
                                            (dom/div #js{:className "pure-menu pure-menu-horizontal custom-menu-3 custom-can-transform"}
                                                     (dom/ul #js{:className "pure-menu-list"}
                                                             (dom/li #js{:className "pure-menu-item"}
                                                                     (dom/a #js{:className "pure-menu-link"
                                                                                :href      "#"
                                                                                :onClick   #(pprint @(core/my-reconciler))} "Help"))))))
                          (ui/ui-tab (om/computed current-tab {:click-cb-fn #(.click-cb this existing-colours %1 %2 %3)}))))))))

(reset! core/app (uc/mount @core/app App "main-app-area"))
(ns om-alarming.components.navigator
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [cljs-time.format :as format-time]
            [cljs-time.core :as time]
            [om-alarming.parsing.mutations.graph]
            [om-alarming.system :as system]
            [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout]]
            [om-alarming.components.log-debug :as ld]
            )
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def date-time-formatter (format-time/formatters :mysql))

(defn calc-begin-time [end span]
  (assert (> span 0) (str "Need +ive span but got: <" span ">"))
  (assert end "No end time")
  (time/minus end (time/seconds span)))

(def sz "huge")
(defn sized [in]
  (str sz " " in))

#_(dom/div #js {:className "ui divider"})

(defn start-stop-system [want-going? line-infos start-millis end-millis graph-chan]
  (let [already-going? (system/going?)]
    (if (and want-going? (not already-going?))
      (system/start! line-infos start-millis end-millis graph-chan)
      (when (and already-going? (not want-going?))
        (system/stop!)))))

(defn to-info [line-query-res]
  (let [system-gas (-> line-query-res :intersect :system-gas)]
    {:ref [:line/by-id (:id line-query-res)]
     :lowest (-> system-gas :lowest)
     :highest (-> system-gas :highest)
     ;:name (-> system-gas :long-name)
     :ident [:gas-at-location/by-id (-> line-query-res :intersect :grid-cell/id)]}))

(defui GraphNavigator
  static om/Ident
  (ident [this props]
    [:navigator/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     :end-time
     :span-seconds
     :receiving?
     ])
  Object
  (debug [this comms-chan]
    (go (>! comms-chan {:cmd :debug-rand-point})))
  (remove-all [this comms-chan]
    (go (>! comms-chan {:cmd :remove-all})))
  (render [this]
    (ld/log-render "GraphNavigator" this)
    (let [{:keys [end-time span-seconds receiving?] :as props} (om/props this)
          {:keys [lines comms-chan]} (om/get-computed this)
          ;_ (println "LINES:\n" lines "\n")
          line-infos (map to-info lines)
          ;_ (println "line-infos: " line-infos)
          formatted-end-time (format-time/unparse date-time-formatter end-time)
          begin-time (calc-begin-time end-time span-seconds)
          formatted-begin-time (format-time/unparse date-time-formatter begin-time)
          play-stop-css (if receiving? "stop icon" "play icon")
          _ (println "Num of lines, start, end: " (count lines) formatted-begin-time formatted-end-time)
          _ (println "RECEIVING: " receiving?)
          _ (start-stop-system receiving? line-infos (.getTime begin-time) (.getTime end-time) comms-chan)
          ]
      (dom/div #js {:className "item"}
               (dom/div #js {:className (sized "ui buttons")}
                        (dom/button #js {:className "ui icon button"
                                         :onClick   (fn [] (.remove-all this comms-chan) (om/transact! this `[(navigate/backwards {:seconds ~span-seconds})]))
                                         :title     (str "Go back " (quot span-seconds 60) " minutes")}
                                    (dom/i #js {:className "left arrow icon"}))
                        (dom/div #js {:className "ui divider"})
                        (dom/button #js {:className "ui icon button"}
                                    (dom/i #js {:className "right arrow icon"
                                                :onClick   (fn [] (.remove-all this comms-chan) (om/transact! this `[(navigate/forwards {:seconds ~span-seconds})]))
                                                :title     (str "Go forward " (quot span-seconds 60) " minutes")}))
                        (dom/div #js {:className "ui divider"})
                        (dom/button #js {:className "ui icon button"
                                         :onClick   (fn [] (.remove-all this comms-chan) (om/transact! this `[(navigate/now)]))
                                         :title     (str "View current " (quot span-seconds 60) " minutes")}
                                    (dom/i #js {:className "sign in icon"}))
                        (dom/div #js {:className "ui divider"})
                        (dom/button #js {:className "ui icon button"
                                         :onClick   (fn [] (om/transact! this `[(graph/toggle-receive {:receiving? ~receiving?})]))
                                         :title     (str "Start receiving for selected gases")}
                                    (dom/i #js {:className play-stop-css}))
                        (dom/div #js {:className "ui divider"})
                        (dom/button #js {:className "ui icon button"
                                         :onClick   (fn [] (.debug this comms-chan))
                                         :title     (str "Will put a random point on the screen")}
                                    (dom/i #js {:className "paw icon"})))
               (dom/div #js {:className "item"}
                        (dom/label #js {:className (sized "ui horizontal label") :style #js {:width 250}}
                                   formatted-begin-time)
                        (dom/label #js {:className (sized "ui horizontal label") :style #js {:width 250}}
                                   formatted-end-time))))))

(def navigator (om/factory GraphNavigator {:keyfn :id}))
#_(format-time/show-formatters)

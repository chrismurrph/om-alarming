(ns om-alarming.components.navigator
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [cljs-time.format :as format-time]
            [cljs-time.core :as time]
            [om-alarming.parsing.mutations.graph]
            [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout]]
            [om-alarming.components.log-debug :as ld])
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

(defn start-stop-system [system-going-fn system-start-fn system-stop-fn want-going? line-infos start-millis end-millis graph-chan]
  (let [already-going? (system-going-fn)]
    (if (and want-going? (not already-going?))
      (system-start-fn line-infos start-millis end-millis graph-chan)
      (when (and already-going? (not want-going?))
        (system-stop-fn)))))

(defn to-info [line]
  (let [intersect (:intersect line)
        system-gas (:system-gas intersect)]
    {:ref [:line/by-id (:id line)]
     :best (:best system-gas)
     :worst (:worst system-gas)
     :display-name (-> intersect :tube :display-name)
     :metric-name (:long-name system-gas)
     :ident [:gas-at-location/by-id (-> line :intersect :grid-cell/id)]}))

(defui Misc
  static om/Ident
  (ident [this props]
    [:misc/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     :system-going-fn
     :system-start-fn
     :system-stop-fn]))

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
     {:graph/misc (om/get-query Misc)}])
  Object
  (debug [this comms-chan]
    (async/put! comms-chan {:cmd :debug-rand-point}))
  (remove-all [this comms-chan]
    (async/put! comms-chan {:cmd :remove-all}))
  (render [this]
    (ld/log-render-on "GraphNavigator" this)
    (let [{:keys [end-time span-seconds receiving? graph/misc] :as props} (om/props this)
          {:keys [system-going-fn system-start-fn system-stop-fn]} misc
          {:keys [lines comms-chan]} (om/get-computed this)
          ;_ (println "LINES:\n" lines "\n")
          line-infos (map to-info lines)
          ;_ (println "line-infos: " line-infos)
          formatted-end-time (format-time/unparse date-time-formatter end-time)
          begin-time (calc-begin-time end-time span-seconds)
          formatted-begin-time (format-time/unparse date-time-formatter begin-time)
          play-stop-css (if receiving? "fa fa-stop" "fa fa-play")
          _ (println "Num of lines, start, end: " (count lines) formatted-begin-time formatted-end-time)
          _ (println "RECEIVING: " receiving?)
          _ (start-stop-system system-going-fn system-start-fn system-stop-fn receiving? line-infos (.getTime begin-time) (.getTime end-time) comms-chan)
          span-minutes (quot span-seconds 60)
          ]
      (dom/div nil
               (dom/label #js{:className "time-label"} formatted-begin-time)
               (dom/button #js{:className "button-xlarge pure-button"
                               :onClick   (fn [] (.remove-all this comms-chan) (om/transact! this `[(navigate/backwards {:seconds ~span-seconds})]))
                               :title     (str "Back " span-minutes " minutes")}
                           (dom/i #js{:className "fa fa-chevron-left"}))
               (dom/button #js{:className "button-xlarge pure-button"
                               :onClick   (fn [] (.remove-all this comms-chan) (om/transact! this `[(navigate/forwards {:seconds ~span-seconds})]))
                               :title     (str "Forward " span-minutes " minutes")}
                           (dom/i #js{:className "fa fa-chevron-right"}))
               (dom/button #js{:className "button-xlarge pure-button"
                               :onClick   (fn [] (.remove-all this comms-chan) (om/transact! this `[(navigate/now)]))
                               :title     (str "View current " span-minutes " minutes")}
                           (dom/i #js{:className "fa fa-caret-square-o-down"}))
               (dom/button #js{:className "button-xlarge pure-button"
                               :onClick   (fn [] (om/transact! this `[(graph/toggle-receive {:receiving? ~receiving?})]))
                               :title     (str (if receiving? "Stop" "Start") " receiving for selected gases")}
                           (dom/i #js{:className play-stop-css}))
               (dom/button #js{:className "button-xlarge pure-button"
                               :onClick   (fn [] (.debug this comms-chan))
                               :title     (str "Will put a random point on the screen")}
                           (dom/i #js{:className "fa fa-paw"}))
               (dom/label #js{:className "time-label"} formatted-end-time)))))

(def navigator (om/factory GraphNavigator {:keyfn :id}))

(ns om-alarming.components.navigator
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [cljs-time.format :as format-time]
            [cljs-time.core :as time]
            [om-alarming.parsing.mutations.navigator]
            [om-alarming.system :as system]))

(def date-time-formatter (format-time/formatters :mysql))

(defn calc-begin-time [end span]
  (assert (> span 0) (str "Need +ive span but got: <" span ">"))
  (assert end "No end time")
  (time/minus end (time/seconds span)))

(def sz "huge")
(defn sized [in]
  (str sz " " in))

#_(dom/div #js {:className "ui divider"})

(defn start-stop [want-going?]
  (let [already-going? (system/going?)]
    (if (and want-going? (not already-going?))
      (system/start!)
      (when (and already-going? (not want-going?))
        (system/stop!)))))

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
  (render [this]
    (let [{:keys [end-time span-seconds receiving?] :as props} (om/props this)
          lines (:lines (om/get-computed this))
          _ (assert lines)
          formatted-end-time (format-time/unparse date-time-formatter end-time)
          begin-time (calc-begin-time end-time span-seconds)
          formatted-begin-time (format-time/unparse date-time-formatter begin-time)
          play-stop-css (if receiving? "stop icon" "play icon")
          _ (println "Num of lines, start, end: " (count lines) formatted-begin-time formatted-end-time)
          _ (start-stop receiving?)
          ]
      (dom/div #js {:className "item"}
               (dom/div #js {:className (sized "ui buttons")}
                        (dom/button #js {:className "ui icon button"
                                         :onClick   #(om/transact! this `[(navigate/backwards {:seconds ~span-seconds})])}
                                    (dom/i #js {:className "left arrow icon"}))
                        (dom/div #js {:className "ui divider"})
                        (dom/button #js {:className "ui icon button"}
                                    (dom/i #js {:className "right arrow icon"
                                                :onClick   #(om/transact! this `[(navigate/forwards {:seconds ~span-seconds})])}))
                        (dom/div #js {:className "ui divider"})
                        (dom/button #js {:className "ui icon button"
                                         :onClick   #(om/transact! this `[(navigate/now)])}
                                    (dom/i #js {:className "sign in icon"}))
                        (dom/div #js {:className "ui divider"})
                        (dom/button #js {:className "ui icon button"
                                         :onClick   (fn [] (om/transact! this `[(graph/toggle-receive {:receiving? ~receiving?})]))}
                                    (dom/i #js {:className play-stop-css})))
               (dom/div #js {:className "item"}
                        (dom/label #js {:className (sized "ui horizontal label") :style #js {:width 250}}
                                   formatted-begin-time)
                        (dom/label #js {:className (sized "ui horizontal label") :style #js {:width 250}}
                                   formatted-end-time))))))

(def navigator (om/factory GraphNavigator {:keyfn :id}))
#_(format-time/show-formatters)

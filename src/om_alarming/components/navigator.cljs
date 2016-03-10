(ns om-alarming.components.navigator
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [cljs-time.format :as format-time]
            [cljs-time.core :as time]))

(def date-time-formatter (format-time/formatters :mysql))

(defn calc-begin-time [end span]
  (assert (> span 0) (str "Need +ive span but got: <" span ">"))
  (assert end "No end time")
  (time/minus end (time/seconds span)))

(def sz "huge")
(defn sized [in]
  (str sz " " in))

#_(dom/div #js {:className "ui divider"})

(defui GraphNavigator
  static om/Ident
  (ident [this props]
    [:navigator/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     :end-time
     :span-seconds])
  Object
  (render [this]
    (let [{:keys [end-time span-seconds] :as props} (om/props this)
          ]
      (dom/div nil #_ #js {:className "item"}
               (dom/div #js {:className (sized "ui buttons")}
                        (dom/button #js {:className "ui icon button"
                                         :onClick #(om/transact! this `[(navigate/backwards {:seconds ~span-seconds})])}
                                    (dom/i #js {:className "left arrow icon"})
                                    )
                        (dom/div #js {:className "ui divider"})
                        (dom/button #js {:className "ui icon button"}
                                    (dom/i #js {:className "right arrow icon"}))
                        (dom/div #js {:className "ui divider"})
                        (dom/button #js {:className "ui icon button"
                                         :onClick #(om/transact! this `[(navigate/forwards {:seconds ~span-seconds})])}
                                    (dom/i #js {:className "sign in icon"}))
                        )
               (dom/div #js {:className "item"}
                        (dom/label #js {:className (sized "ui horizontal label") :style #js {:width 250}}
                               (format-time/unparse date-time-formatter (calc-begin-time end-time span-seconds)))
                        (dom/label #js {:className (sized "ui horizontal label") :style #js {:width 250}}
                               (format-time/unparse date-time-formatter end-time)))))))

(def navigator (om/factory GraphNavigator {:keyfn :id}))
#_(format-time/show-formatters)

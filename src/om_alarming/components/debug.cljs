(ns om-alarming.components.debug
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.reconciler :as reconciler]
            ))

(defui Debug
  static om/IQuery
  (query [_]
    '[[:graph/receiving? _]])
  Object
  (render [this]
    (let [props (om/props this)
          _ (println "props:" props)
          {:keys [graph/receiving?]} props]
      (dom/div nil 
               (dom/button #js {:onClick #(reconciler/alteration 'graph/toggle-receive nil :graph/receiving?)} "Receive toggle")
               (str "   Whether receiving:" receiving?)))))
(def debug (om/factory Debug))

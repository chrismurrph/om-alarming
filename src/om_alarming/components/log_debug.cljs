(ns om-alarming.components.log-debug
  (:require [om.next :as om]))

(def debug-on? true)

(defn log-render [name component]
  (when debug-on?
    (println "RENDER:" name (:id (om/props component)))))

(defn log-mutation
  ([k params]
   (when debug-on?
     (println "MUTATION:" k params)))
  ([k]
   (log-mutation k nil)))

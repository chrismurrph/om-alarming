(ns om-alarming.components.log-debug
  (:require [om.next :as om]))

(def debug-on? true)

(defn log-render
  ([name component id-fn]
   (when debug-on?
     (let [props (om/props component)
           _ (assert (map? props) (str "props of a component must always be a map, got:" (type props)))
           id (or (get props id-fn) "<no id prop>")
           prop-ids (map str (keys props))]
       (println "RENDER:" name id prop-ids))))
  ([name component]
    (log-render name component :id)))

(defn log-mutation
  ([k params]
   (when debug-on?
     (println "MUTATION:" k (or params ""))))
  ([k]
   (log-mutation k nil)))

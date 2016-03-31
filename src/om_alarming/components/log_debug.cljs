(ns om-alarming.components.log-debug
  (:require [om.next :as om]))

(def debug-on? false)

(defn log-render [name component]
  (when debug-on?
    (let [props (om/props component)
          _ (assert (map? props) (str "props of a component must always be a map, got:" (type props)))
          id (or (:id props) "<no id prop>")
          prop-ids (map str (keys props))]
      (println "RENDER:" name id prop-ids))))

(defn log-mutation
  ([k params]
   (when debug-on?
     (println "MUTATION:" k (or params ""))))
  ([k]
   (log-mutation k nil)))

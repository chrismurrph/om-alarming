(ns om-alarming.system
  (:require
    [taoensso.timbre :as timbre]
    [com.stuartsierra.component :as component]
    [om-alarming.core :as core]))

(defn logging-mutate [env k params]
  (timbre/info "Mutation Request: " k)
  ;(api/apimutate env k params)
  )

(defrecord Database [name]
  component/Lifecycle

  (start [component]
    (println "Starting " name)
    (core/start!))
  (stop [component]
    (println "Stopping " name)
    (core/stop!))
  )

(defn new-database [name]
  (map->Database {:name name}))

(defn make-system []
  (let [_ (println "In make-system. Server is here!!!!")]
    (new-database "My DB")))

(ns om-alarming.system
  (:require
    [taoensso.timbre :as timbre]
    [taoensso.timbre.appenders.core :as appenders]
    [com.stuartsierra.component :as component]
    [om-alarming.core :as core]
    [clojure.java.io :as io]))

; Set up the name of the log output file and delete any contents from previous runs (the
; default is to continually append all runs to the file).
(def log-file-name "timbre.log")
(io/delete-file log-file-name :quiet)

;; The default setup is simple console logging.  We with to turn off console logging and
;; turn on file logging to our chosen filename.
;(timbre/set-config! [:appenders :standard-out   :enabled?] false)
;(timbre/set-config! [:appenders :spit           :enabled?] true)
;(timbre/set-config! [:shared-appender-config :spit-filename] log-file-name)
;(timbre/set-config! [:shared-appender-config :spit-filename] log-file-name)

(timbre/merge-config!
  {:appenders {:println {:enabled? false}
               :spit (appenders/spit-appender {:fname log-file-name})}})

; Just use info because this doesn't work
; Set the lowest-level to output as :debug
;(timbre/set-level! :debug)

(defn logging-mutate [env k params]
  (timbre/info "Mutation Request: " k)
  ;(api/apimutate env k params)
  )

(defrecord Database [name]
  component/Lifecycle

  (start [component]
    (timbre/info "Starting " name)
    (core/start!))
  (stop [component]
    (timbre/info "Stopping " name)
    (core/stop!))
  )

(defn new-database [name]
  (map->Database {:name name}))

(defn make-system []
  (let [_ (timbre/info "In make-system. Server is here!!!!")]
    (new-database "My DB")))

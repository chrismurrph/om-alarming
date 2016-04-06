(ns om-alarming.system
  (:require
    [taoensso.timbre :as timbre]
    ))

(defn logging-mutate [env k params]
  (timbre/info "Mutation Request: " k)
  ;(api/apimutate env k params)
  )

(defn make-system []
  (let [config-path "/usr/local/etc/todomvc.edn"
        _ (println "In make-system. Server is here!!!!")]
    #_(core/make-untangled-server
      :config-path config-path
      :parser (om/parser {:read api/api-read :mutate logging-mutate})
      :parser-injections #{:todo-database}
      :components {:todo-database (build-database :todo)})))

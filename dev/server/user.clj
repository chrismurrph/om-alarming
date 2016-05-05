(ns user
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :refer (pprint)]
    [clojure.stacktrace :refer (print-stack-trace)]
    [clojure.tools.namespace.repl :refer [disable-reload! refresh clear set-refresh-dirs]]
    [com.stuartsierra.component :as component]
    [figwheel-sidecar.repl-api :as ra]
    [taoensso.timbre :refer [info set-level!]]
    [om-alarming.system :as system]
    ))

;;FIGWHEEL

(def figwheel-config
  {:figwheel-options {:open-file-command "open-in-intellij"
                      :css-dirs ["resources/public/css"]
                      :server-port 2345}
   :build-ids        ["dev"]
   :all-builds       (figwheel-sidecar.repl/get-project-cljs-builds)})

(defn start-figwheel
  "Start Figwheel on the given builds, or defaults to build-ids in `figwheel-config`."
  ([]
   (let [all-the-builds (->> figwheel-config :all-builds)
         all-build-ids (mapv :id all-the-builds)
         _ (println "All builds: " all-build-ids)
         ;_ (pprint all-the-builds)
         ]
     (start-figwheel (keys (select-keys (System/getProperties) all-build-ids)))))
  ([build-ids]
   (let [default-build-ids (:build-ids figwheel-config)
         build-ids (if (empty? build-ids) default-build-ids build-ids)]
     (println "STARTING FIGWHEEL ON BUILDS: " build-ids)
     (ra/start-figwheel! (assoc figwheel-config :build-ids build-ids))
     (ra/cljs-repl))))

;;SERVER

(set-refresh-dirs "dev/server" "src/server")

(defonce system (atom nil))

(set-level! :info)

(defn init
  "Create a web server from configurations. Use `start` to start it."
  []
  (reset! system (system/make-system)))

(defn start "Start (an already initialized) web server." []
  (swap! system component/start))
(defn stop "Stop the running web server." []
  ;(println "In stop and stop function is " component/stop)
  (swap! system component/stop)
  (reset! system nil))

(defn go "Load the overall web server system and start it." []
  (init)
  (start))

(defn reset
  "Stop the web server, refresh all namespace source code from disk, then restart the web server."
  []
  (stop)
  (refresh :after 'user/go))

(def data '([{:time "05_05_2016" :val 0.2}{:time "05_05_2017" :val 0.3}] [] [{:time "05_05_2016" :val 0.5}{:time "05_05_2017" :val 0.6}]))
(def infos [{:best 1 :worst 2 :name "Methane"}{:best 5 :worst 6 :name "Oxygen"}{:best 9 :worst 10 :name "Vacuum"}])

(defn test-me []
  (let [first-res (map (fn [datum info] (into {} [[:info info] [:points datum]])) data infos)]
    first-res))




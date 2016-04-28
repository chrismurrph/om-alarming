(defproject om-alarming "0.1.0"
  :description "Alarming and Trending"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.40"]
                 [org.clojure/core.async "0.2.371"]
                 [org.clojure/core.match "0.2.1"]
                 [org.omcljs/om "1.0.0-alpha32"]            ;; <- 33-SNAPSHOT is latest
                 [cljsjs/react "0.14.3-0"]
                 [cljsjs/react-dom "0.14.3-1"]
                 [devcards "0.2.1-4"]
                 [org.clojure/test.check "0.9.0"]
                 [default-db-format "0.1.1-SNAPSHOT"]
                 [com.andrewmcveigh/cljs-time "0.3.14"]
                 [cljsjs/d3 "3.5.7-1"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [com.taoensso/timbre "4.3.1"]
                 [enhanced-smartgas-deps "1.0.0"]
                 [org.clojure/core.async "0.2.374"]
                 [com.taoensso/sente "1.8.0"]
                 [http-kit "2.1.21-alpha2"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]               ; Includes `ring-anti-forgery`, etc.
                 [compojure "1.4.0"]                        ; Or routing lib of your choice
                 [com.cognitect/transit-clj "0.8.285"]
                 [com.cognitect/transit-cljs "0.8.237"]
                 [navis/untangled-client "0.4.8"]
                 ]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-less "1.7.5"]]

  :clean-targets ^{:protect false} ["resources/public/js/" "target"]

  :source-paths ["src/client" "src/server" "test" "dev/server"]
  :java-source-paths ["java/src"]

  :less {:source-paths ["less/app.main.less"]
         :target-path "resources/public/css/app.css"}

  :cljsbuild {:builds [{:id "test"
                        :source-paths ["test"] ;; <- usually "test", sometimes "src"
                        :figwheel true
                        :compiler {:main       "misc.autocomplete"
                                   :asset-path "js/test"
                                   :output-to  "resources/public/js/main.js"
                                   :output-dir "resources/public/js/test"
                                   :source-map-timestamp true }}
                       {:id "dev"
                        :source-paths ["src/client"]
                        :figwheel true ;; We want to be connected to a real server rather than a figwheel one
                                       ;; That not what this option is about - it is about whether figwheel code
                                       ;; is included, so that the reload request is acted on.
                        :compiler {
                                   :main       "om-alarming.root"
                                   :asset-path "js/out"
                                   :output-to  "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :source-map-timestamp true }}
                       {:id "devcards"
                        :source-paths ["src/client"]
                        :figwheel { :devcards true }
                        :compiler { :main       "cards.ui"
                                    :asset-path "js/devcards_out"
                                    :output-to  "resources/public/js/devcards.js"
                                    :output-dir "resources/public/js/devcards_out"
                                    :source-map-timestamp true }}]}

  :figwheel {:open-file-command "open-in-intellij"
             :css-dirs ["resources/public/css"]
             :server-port 2345}

  :repositories [["localrepo1" {:url "file:///home/chris/IdeaProjects/om-alarming"
                                :username :env/localrepo_username
                                :password :env/localrepo_password}]] 

  :profiles {
             :dev {
                   :repl-options {
                                  :init-ns          user
                                  :port             7001
                                  }
                   :env          {:dev true}
                   :dependencies [[figwheel-sidecar "0.5.0-6"]
                                  [binaryage/devtools "0.5.2" :exclusions [environ]]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.12"]]}}
  )

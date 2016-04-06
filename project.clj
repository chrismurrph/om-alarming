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
                 [com.taoensso/timbre "4.3.0"]
                 [smartgas-deps "1.0.0"]
                 [org.clojure/core.async "0.2.374"]
                 [com.taoensso/sente "1.8.0"]
                 [http-kit "2.1.21-alpha2"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]               ; Includes `ring-anti-forgery`, etc.
                 [compojure "1.4.0"]                        ; Or routing lib of your choice
                 [com.cognitect/transit-clj "0.8.285"]
                 [com.cognitect/transit-cljs "0.8.237"]
                 ]

  :plugins [[lein-cljsbuild "1.1.2"]]

  :clean-targets ^{:protect false} ["resources/public/js/" "target"]

  :source-paths ["src/client" "src/server" "test" "dev/server"] ;;-> Will get rid of script b/c it s/only contain figwheel
  :java-source-paths ["java/src"]

  :cljsbuild {:builds [{:id "test"
                        :source-paths ["test"] ;; <- usually "test", sometimes "src"
                        :figwheel true
                        :compiler {:main       "misc.lines"
                                   :asset-path "js/test"
                                   :output-to  "resources/public/js/main.js"
                                   :output-dir "resources/public/js/test"
                                   :source-map-timestamp true }}
                       {:id "dev"
                        :source-paths ["src/client"]
                        :figwheel true
                        :compiler {
                                   :main       "om-alarming.core"
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
             :css-dirs ["resources/public/css"] }

  :profiles {
             :dev {
                   :repl-options {
                                  :init-ns          user
                                  :port             7001
                                  }
                   :env          {:dev true}
                   :dependencies [[figwheel-sidecar "0.5.0-6"]
                                  [binaryage/devtools "0.5.2" :exclusions [environ]] ;;
                                  [org.clojure/tools.nrepl "0.2.12"]]}}
  )
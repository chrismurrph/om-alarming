(defproject om-alarming "0.1.0"
  :description "Alarming and Trending"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.371"]
                 [org.clojure/core.match "0.2.1"]
                 [org.omcljs/om "1.0.0-alpha31-SNAPSHOT"]
                 [cljsjs/react "0.14.3-0"]
                 [cljsjs/react-dom "0.14.3-1"]
                 [devcards "0.2.1-4"]
                 [org.clojure/test.check "0.9.0"]
                 [default-db-format "0.1.1-SNAPSHOT"]
                 [com.andrewmcveigh/cljs-time "0.3.14"]
                 ]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-figwheel "0.5.0-1"]]

  :clean-targets ^{:protect false} ["resources/public/js/"
                                    "target"]
                                    
  :source-paths ["src" "test"]
                 
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["test"]
                        :figwheel true
                        :compiler {:main       "kanban.tree-db"
                                   :asset-path "js/test"
                                   :output-to  "resources/public/js/main.js"
                                   :output-dir "resources/public/js/test"
                                   :source-map-timestamp true }}
                       {:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {
                                   :main       "om-alarming.core"
                                   :asset-path "js/out"
                                   :output-to  "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :source-map-timestamp true }}
                       {:id "devcards"
                        :source-paths ["src"]
                        :figwheel { :devcards true }
                        :compiler { :main       "cards.ui"
                                    :asset-path "js/devcards_out"
                                    :output-to  "resources/public/js/devcards.js"
                                    :output-dir "resources/public/js/devcards_out"
                                    :source-map-timestamp true }}]}

  :figwheel { :css-dirs ["resources/public/css"] })
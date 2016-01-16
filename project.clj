(defproject om-alarming "0.1.0-SNAPSHOT"
  :description "Alarming and Trending"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.omcljs/om "1.0.0-alpha24"]
                 [cljsjs/react "0.14.3-0"]
                 [cljsjs/react-dom "0.14.3-1"]
                 ;[figwheel-sidecar "0.5.0-SNAPSHOT" :scope "test"]
                 ]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-figwheel "0.5.0-1"]]

  :clean-targets ^{:protect false} ["resources/public/js/"
                                    "target"]
                                    
  :source-paths ["src"]                                    
                 
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main       "om-alarming.core"
                                   :asset-path "js/out"
                                   :output-to  "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :source-map-timestamp true }}]}

  :figwheel { :css-dirs ["resources/public/css"] })
#rlwrap lein figwheel dev
JVM_OPTS="-Ddev" rlwrap lein run -m clojure.main script/figwheel.clj -r

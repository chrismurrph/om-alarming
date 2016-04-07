#rlwrap lein figwheel test
JVM_OPTS="-Dtest" rlwrap lein run -m clojure.main script/figwheel.clj -r

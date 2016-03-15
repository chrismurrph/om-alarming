(ns om-alarming.system
  (:require [cljs.core.async :as async :refer [<! timeout chan alts! put!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn make-printer-component []
  (println "[printer-component] starting")
  (let [poison-ch (chan)]
    (go-loop [count 0]
             (let [[v ch] (alts! [poison-ch (timeout 1000)])]
               (if (= ch poison-ch)
                 (println "[printer-component] stopping")
                 (do
                   (println "In timer at " count)
                   (recur (inc count))))))
    (fn stop! []
      (put! poison-ch :stop))))

(defn make-system! []
  (println "[system] starting")
  (let [components [(make-printer-component)]]
    (fn stop! []
      (println "[system] stopping")
      (doseq [component components]
        (component)))))

(defonce system (atom nil))

(defn stop! []
  (when-let [stop-system! @system]
    (stop-system!)))

(defn start! []
  (stop!)
  (reset! system (make-system!)))

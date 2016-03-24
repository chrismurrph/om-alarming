(ns om-alarming.system
  (:require
    [cljs.core.async :as async :refer [<! timeout chan put! close! alts!]]
    [om-alarming.graph.incoming :as in]
    [om-alarming.graph.staging-area :as sa]
    [om-alarming.reconciler :as reconciler])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def uniqkey (atom 0))
(defn gen-uid []
  (let [res (swap! uniqkey inc)]
    ;(u/log res)
    res))

(defn make-printer-component []
  (println "[printer-component] starting")
  (let [poison-ch (chan)]
    (go-loop [count 0]
             (let [[_ ch] (alts! [poison-ch (timeout 1000)])]
               (if (= ch poison-ch)
                 (println "[printer-component] stopping")
                 (do
                   (println "In timer at " count)
                   (recur (inc count))))))
    (fn stop! []
      (close! poison-ch))))

(defn make-outer-chan [line-infos start-millis end-millis]
  (in/query-remote-server line-infos start-millis end-millis))

(defn make-inner-chan [line-infos week-ago-millis now-millis outer-chan]
  (sa/show-component line-infos week-ago-millis now-millis outer-chan))

(defn point-adding-component [inner-chan graph-chan]
  (println "[point-adding-component] starting")
  (let [poison-ch (chan)]
    (go-loop [counted-to 0]
             (let [[{:keys [info point]} ch] (alts! [poison-ch inner-chan])]
               (if (= ch poison-ch)
                 (println "[point-adding-component] stopping")
                 (let [paused? (not (:receiving? (:graph/navigator (reconciler/internal-query [{:graph/navigator [:receiving?]}]))))
                       [x y val] point
                       line-ident (:ref info)
                       ;_ (println "Ident: " line-ident)
                       _ (assert line-ident)]
                   (if (and (< counted-to 40) (not paused?))
                     (do
                       (go (>! graph-chan {:cmd :new-point :value {:x x :y y :val val :point-id (gen-uid)} :line line-ident}))
                       #_(reconciler/alteration 'graph/add-point
                                              {:line-name-ident line-ident :x x :y y :val val}
                                              :graph/points)
                       ;(println "Receiving " name x y)
                       (recur (inc counted-to)))
                     (recur counted-to))))))
    (fn stop! []
      (close! poison-ch))))

;;
;; Dereference this to get the stopping function that will stop the system
;;
(defonce system (atom nil))

(defn make-system-container! [line-infos start-millis end-millis graph-chan]
  (println "[system] starting")
  (let [[stop-fn-outer outer-chan] (make-outer-chan line-infos start-millis end-millis)
        [stop-fn-inner inner-chan] (make-inner-chan line-infos start-millis end-millis outer-chan)
        components [(make-printer-component) stop-fn-inner stop-fn-outer (point-adding-component inner-chan graph-chan)]
        ;_ (println "Num of components in started system is " (count components))
        ]
    (fn stop! []
      (println "[system] stopping")
      (reset! system nil)
      (doseq [component components]
        (component)))))

(defn stop! []
  (when-let [stop-system! @system]
    (stop-system!)))

(defn going? []
  (not= @system nil))

(defn start! [line-infos start-millis end-millis graph-chan]
  (stop!)
  (reset! system (make-system-container! line-infos start-millis end-millis graph-chan)))

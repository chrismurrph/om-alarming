(ns om-alarming.reconciler
  (:require [goog.object :as gobj]
            [om.next :as om]
            [om-alarming.state :refer [initial-state]]
            [clojure.string :as str]))

(defmulti read om/dispatch)

(defmulti mutate om/dispatch)

(def my-parser
  (om/parser {:read read
              :mutate mutate}))

(deftype Logger []
  Object
  (info [this msg ex]
    ; It is pretty hard to get into what's returned so leaving for now
    ;(println "INFO" msg)
    )
  (warning [this msg ex] (println "WARN" msg)))
(def logger (Logger.))

;;
;; We are not passing in an atom so normalization WILL happen by default. Thus:
;; `:normalize true` is just for documentation purposes. But it is important
;; because our reads use db->tree, which works with normalized state.
;;
(def my-reconciler
  (om/reconciler {:normalize true
                  :state initial-state
                  :parser my-parser
                  :logger logger}))

;; (in-ns 'om-alarming.reconciler)
(defn query [kw]
  ;(println "External query for " kw)
  (let [res (my-parser {:state my-reconciler} `[[~kw _]])]
    (when (not-empty res) (apply val res))))

;; (change 'app/tab {:new-id 1} :app/selected-button)
(defn alteration [mutate-key param-map kw]
  (om/transact! my-reconciler `[(~mutate-key ~param-map) ~kw])
  )


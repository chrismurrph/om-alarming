(ns om-alarming.core
  (:require [goog.events :as events]
            [goog.dom :as gdom]
    ;[om.next :as om :refer-macros [defui]]
    ;[om.dom :as dom]
            [om-training.utils :as u]))

(enable-console-print!)

;; -----------------------------------------------------------------------------
;; Components (clj->js )


;(def reconciler
;  (om/reconciler
;    {:state     (atom initial-state)
;     :normalize true
;     :parser    (om/parser {:read p/read :mutate p/mutate})
;     :send      (util/no-send "/api")
;     })
;  )
;
;(defui SayHello
;  Object
;  (render [this]
;    (dom/h1 nil "Hi there")))
;
;(om/add-root! reconciler SayHello (gdom/getElement "app"))

(println "Hi???")

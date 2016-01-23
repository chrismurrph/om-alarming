(ns om-alarming.core
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.reconciler :refer [reconciler]]
            [om-alarming.parsing.app :as app]
            [om-alarming.utils :as u]))

(enable-console-print!)

;; -----------------------------------------------------------------------------
;; Components (clj->js )

(defui Gas
  static om/Ident
  (ident [this props]
    [:gas/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :name]))

;(def reconciler
;  (om/reconciler
;    {:state     (atom initial-state)
;     :normalize true
;     :parser    (om/parser {:read p/read :mutate p/mutate})
;     :send      (util/no-send "/api")
;     })
;  )
;
(defui App
  static om/IQuery
  (query [this]
    [{:app/gases (om/get-query Gas)}])
  Object
  (render [this]
    (let [props (om/props this)
          {:keys [app/gases]} props]
      (dom/h1 nil (str "Howdy there partner, gases are " (map :name gases))))))
;
;(om/add-root! reconciler SayHello (gdom/getElement "app"))

(defn run []
  (om/add-root! reconciler
                App
                (.. js/document (getElementById "main-app-area"))))

(run)

(ns misc.uwo
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui Component
       static om/IQueryParams
       (params [this]
               {:term ""})
       static om/IQuery
       (query [this]
              '[({:search/results [:some/name]} {:term ?term})])
       Object
       (render [this]
               (let [{:keys [search/results]} (om/props this)]
                 (dom/div nil
                          (dom/input #js {:onChange #(om/set-query! this {:params {:term (.. e -target -value)}})})
                          (apply dom/ul nil
                                 (map (fn [s] (dom/li nil (:some/name s))) results))))))

(def component (om/factory Component))

(defui Root
       Object
       (render [this]
               (component)))

(defmulti read om/dispatch)

(defmethod read :search/results
  [{:keys [state]} k {:keys [term]}]
  (let [st @state]
    (if (= term "")
      {:value []}
      (merge {:value (get st k [])}
             (when (>= (count term) 3)
               {:remote true})))))

;;assume a server endpoint and parser that returns a list

#_(defn transit-post [url]
  (fn [{:keys [remote]} cb]
    (.send XhrIo url
           (fn [e]
             (this-as this
               (cb (t/read (t/reader :json) (.getResponseText this)))))
           "POST" (t/write (t/writer :json) remote)
           #js {"Content-Type" "application/transit+json"})))

#_(def reconciler
  (om/reconciler
    {:state     (atom {})
     :normalize true
     :parser    (om/parser {:read p/read})
     :send      (util/transit-post "/om-api")}))

#_(let [target (js/document.getElementById "app")]
  (om/add-root! reconciler Root target))

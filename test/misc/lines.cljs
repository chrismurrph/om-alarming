(ns misc.lines
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [default-db-format.core :as db-format]
            [cljs.pprint :refer [pprint]]
            [om-alarming.util.utils :as u]
            [om-alarming.misc.lines-help :as help]
            [om-alarming.util.colours :refer [pink green blue red]]
            [om-alarming.util.colours :as colours]))

(enable-console-print!)

(def okay-val-maps #{[:r :g :b]})
(def check-config {:okay-value-maps
                   okay-val-maps})

(def init-state
  {:app/tubes
   [{:id    1000
     :tube-num 1}]
   :graph/lines
              [
               {:id        100
                :colour    pink
                :intersect {:grid-cell/id 500}}
               {:id        101
                :colour    green
                :intersect {:grid-cell/id 501}}
               {:id        102
                :colour    blue
                :intersect {:grid-cell/id 503}}
               {:id        103
                :colour    red
                :intersect {:grid-cell/id 502}}
               ]
   :tube/real-gases
              [{:grid-cell/id 500
                :system-gas   {:id 150}
                :tube         {:id 1000}}
               {:grid-cell/id 501
                :system-gas   {:id 151}
                :tube         {:id 1000}}
               {:grid-cell/id 502
                :system-gas   {:id 152}
                :tube         {:id 1000}}
               {:grid-cell/id 503
                :system-gas   {:id 153}
                :tube         {:id 1000}}]
   :app/sys-gases [{:id     150 :long-name "Methane" :short-name "CH\u2084"
                :best 0.25 :worst 1 :units "%"}
               {:id     151 :long-name "Oxygen" :short-name "O\u2082"
                :best 19 :worst 12 :units "%"}
               {:id     152 :long-name "Carbon Monoxide" :short-name "CO"
                :best 30 :worst 55 :units "ppm"}
               {:id     153 :long-name "Carbon Dioxide" :short-name "CO\u2082"
                :best 0.5 :worst 1.35 :units "%"}]
   :app/customers
              [{:id         200
                :first-name "Greg"}
               {:id         201
                :first-name "Sally"}
               {:id         202
                :first-name "Ben"}
               ]
   }
  )

(defn check-default-db [st]
  (let [version db-format/version
        check-result (db-format/check check-config st)
        ok? (db-format/ok? check-result)
        msg-boiler (str "normalized (default-db-format ver: " version ")")
        message (if ok?
                  (str "GOOD: state fully " msg-boiler)
                  (str "BAD: state not fully " msg-boiler))]
    (println message)
    (when (not ok?)
      (pprint st)
      (db-format/show-hud check-result))))

(defui SystemGas
  static om/Ident
  (ident [this props]
    [:gas-of-system/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     :short-name
     :best
     :worst
     :long-name]))

(defui Location
  static om/Ident
  (ident [this props]
    [:location/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     :tube-num]))

(defui GraphLineSelectionCheckbox
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:grid-cell/id props)])
  static om/IQuery
  (query [this]
    [:grid-cell/id
     :value
     {:tube (om/get-query Location)}
     {:system-gas (om/get-query SystemGas)}])
  Object
  (render [this]
    (let [{:keys [grid-cell/id]} (om/props this)
          ;_ (println "Potential line id: " id)
          {:keys [selected?]} (om/get-computed this)
          _ (println "Rendering cb:" id "when selected is:" selected?) ;; <- whole point of the exercise
          ]
      (dom/div #js {:className "switch demo3"}
               (dom/input #js{:type    "checkbox"
                              :checked selected?
                              :onClick (fn [e]
                                         (let [action (.. e -target -checked)]
                                           (println "Pressed so attempting to set to:" action)
                                           (om/transact! this `[(graph/select-gas {:want-to-select? ~action :grid-cell/id ~id}) :graph/lines])))})
               (dom/label nil (dom/i nil))))))
(def graph-line-selection-checkbox (om/factory GraphLineSelectionCheckbox {:keyfn :grid-cell/id}))

(defui Line
  static om/Ident
  (ident [this props]
    [:line/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     :colour
     {:intersect (om/get-query GraphLineSelectionCheckbox)}]))

(defui FakeGraph
  Object
  (render [this]
    (println "Rendering the FakeGraph")
    (let [lines (om/props this)
          selected-gases (map #(-> % :intersect :system-gas :long-name) lines)]
      (dom/label nil (apply str "Graph for these: " (interpose ", " selected-gases))))))
(def fake-graph (om/factory FakeGraph))

(defui Customer
  static om/Ident
  (ident [this props]
    [:customer/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id :first-name]))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)
(def parser
  (om/parser {:read   read
              :mutate mutate}))

(defmethod read :tube/real-gases
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :graph/lines
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :app/customers
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :app/sys-gases
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defmethod read :app/tubes
  [{:keys [state query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)}))

(defn new-line
  "Modifies the state in two places - so perfectly puts in a new line.
  Caller needs to work on this state a little more to put the line in
  an existing graph"
  [st colour intersect-id]
  ;(println "Look at" (count (get st :graph/lines)) " lines, new one to be " colour)
  (let [id   (->> (om/db->tree [:id] (get st :graph/lines) st)
                  (map :id)
                  (cons 99)
                  (reduce max)
                  inc)
        ;_ (println "In new-line, new id is " id)
        line {:id id :intersect [:gas-at-location/by-id intersect-id] :colour colour}
        ref  [:line/by-id id]]
    {:line-ident ref
     :state (-> st
                (assoc-in ref line)
                (update :graph/lines conj ref))}))

(defn delete-line [st intersect-id]
  (let [intersect-ident [:gas-at-location/by-id intersect-id]
        line-id (:id (u/first-only (filter (fn [v] (= intersect-ident (:intersect v))) (vals (get st :line/by-id)))))
        line-ident [:line/by-id line-id]
        ]
    {:line-ident line-ident
     :state      (-> st
                     (update :graph/lines u/vec-remove-value line-ident)
                     (update :line/by-id u/unselect-keys [line-id])
                     )}))

(defn rem-line [st params]
  (let [{:keys [graph-ident intersect-id]} params
        {:keys [state line-ident]} (delete-line st intersect-id)]
    (if graph-ident
      (-> state
          (update-in (conj graph-ident :graph/lines) u/vec-remove-value line-ident))
      state)))

;;
;; To create a line we need to know the name of the graph that it is to go in
;; and colour and id of its intersect.
;;
(defn create-line [st params]
  (let [{:keys [graph-ident colour intersect-id]} params]
    (if colour
      (let [{:keys [state line-ident]} (new-line st colour intersect-id)]
        (if graph-ident
          (-> state
              (update-in (conj graph-ident :graph/lines) conj line-ident))
          state))
      st)))

;;
;; "Only need to add or remove from the :graph/selected-lines refs mapentry"
;; (pprint (get @state :graph/selected-lines))
;;
(defmethod mutate 'graph/select-gas
  [{:keys [state]} _ {:keys [want-to-select? grid-cell/id]}]
  {:action #(if want-to-select?
             (swap! state create-line {:intersect-id id :colour (colours/new-random-colour)})
             (swap! state rem-line {:intersect-id id}))})

(def my-reconciler
  (om/reconciler {:normalize true                           ;; -> documentation
                  :state     init-state
                  :parser    parser}))

(defui Root
  static om/IQuery
  (query [this]
    [{:tube/real-gases (om/get-query GraphLineSelectionCheckbox)}
     {:graph/lines (om/get-query Line)}
     {:app/customers (om/get-query Customer)}
     {:app/sys-gases (om/get-query SystemGas)}
     {:app/tubes (om/get-query Location)}
     ])
  Object
  (render [this]
    (println "Rendering 'misc.lines' from Root")
    (let [{:keys [tube/real-gases graph/lines]} (om/props this)]
      (dom/div nil
               (check-default-db @my-reconciler)
               (map #(graph-line-selection-checkbox (om/computed % {:selected? (boolean (some #{%} (map :intersect lines)))})) real-gases)
               (fake-graph lines)
               (dom/br nil)
               (dom/br nil)
               (help/any-action {:text "Show State" :action #(pprint @my-reconciler)})
               (dom/br nil)
               #_(help/any-action {:text "Add Selection" :action #(help/mutate help/norm-state true 504)})
               #_(help/any-action {:text "Remove Selection" :action #(help/mutate help/norm-state false 500)})
               ))))

(defn run []
  (om/add-root! my-reconciler
                Root
                (.. js/document (getElementById "main-app-area"))))

(run)

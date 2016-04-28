(ns om-alarming.components.grid
  (:require [om.next :as om :refer-macros [defui]]
            [om-alarming.components.general :as gen]
            [om.dom :as dom]
            [om-alarming.components.log-debug :as ld]
            [om-alarming.components.graphing :as graph]
            [cljs.pprint :refer [pprint]]))

(defui GridDataCell
  static om/Ident
  (ident [this props]
    [:gas-at-location/by-id (:grid-cell/id props)])
  static om/IQuery
  (query [this]
    [:grid-cell/id
     :selected?
     {:system-gas (om/get-query gen/SystemGas)}
     {:tube (om/get-query gen/Location)}])
  Object
  (pick [this click-cb-fn id selected?]
    (assert id)
    (assert (not (nil? selected?)))
    (click-cb-fn this id selected?))
  (render [this]
    (ld/log-render-off "GridDataCell" this :grid-cell/id)
    (let [{:keys [grid-cell/id system-gas tube selected?] :as props} (om/props this)
          _ (assert id)
          _ (assert (not (nil? selected?)))
          {:keys [tube-num click-cb-fn]} (om/get-computed this)
          _ (assert click-cb-fn "GridDataCell")
          ]
      (dom/label #js {:className "pure-checkbox" :htmlFor "gdc"}
                 (dom/input #js {:id "gdc"
                                 :type    "checkbox"
                                 :checked (boolean selected?) ;; <- Note boolean function - js needs it!
                                 :onClick (fn [_] (.pick this click-cb-fn id selected?))}) ""))))
(def grid-data-cell-component (om/factory GridDataCell {:keyfn :grid-cell/id}))

(defui GasQueryGrid
  static om/Ident
  (ident [this props]
    [:gas-query-grid/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     {:tube/real-gases (om/get-query GridDataCell)}
     {:app/sys-gases (om/get-query gen/SystemGas)}
     ])
  Object
  (render [this]
    (ld/log-render-off "GasQueryGrid" this)
    (let [props (om/props this)
          {:keys [id tube/real-gases app/sys-gases]} props
          hdr-gases (map :short-name (into [{:id 0 :short-name "Tube"}] sys-gases))
          _ (assert id)
          _ (assert real-gases "no real-gases inside GasQueryGrid")
          _ (println "FIRST:" (first real-gases))
          _ (pprint hdr-gases)
          {:keys [click-cb-fn]} (om/get-computed this)
          all-hdr-and-gases (map-indexed #(into [{:hdr-tube-num (inc %1)}] %2) (partition (count sys-gases) real-gases))]
      (dom/table #js{:className "pure-table pure-table-striped"}
                 (dom/thead nil (apply dom/tr nil (map (fn [name] (dom/th nil name)) hdr-gases)))
                 (dom/tbody nil
                            (for [hdr-and-gases all-hdr-and-gases
                                  :let [key (:hdr-tube-num (first hdr-and-gases))
                                        _ (println (str "key: " key))]]
                              (apply dom/tr #js{:key key}
                                     (map (fn [cell] (let [tube-num (:hdr-tube-num cell)]
                                                       (dom/td nil (if tube-num
                                                                     tube-num
                                                                     (grid-data-cell-component (om/computed cell {:click-cb-fn click-cb-fn})))))) hdr-and-gases))))))))
(def gas-query-grid-component (om/factory GasQueryGrid {:keyfn :id}))

(comment (dom/table #js{:className "pure-table pure-table-striped"}
                    (dom/thead nil
                               (dom/tr nil
                                       (dom/th nil "Tube #")
                                       (dom/th nil "CH4")
                                       (dom/th nil "O2")
                                       (dom/th nil "CO")
                                       (dom/th nil "CO2")))
                    (dom/tbody nil
                               (dom/tr nil
                                       (dom/td nil "1")
                                       (dom/td nil (dom/input #js{:type "checkbox"}))
                                       (dom/td nil (dom/input #js{:type "checkbox"}))
                                       (dom/td nil (dom/input #js{:type "checkbox"}))
                                       (dom/td nil (dom/input #js{:type "checkbox"})))
                               (dom/tr nil
                                       (dom/td nil "2")
                                       (dom/td nil (dom/input #js{:type "checkbox"}))
                                       (dom/td nil (dom/input #js{:type "checkbox"}))
                                       (dom/td nil (dom/input #js{:type "checkbox"}))
                                       (dom/td nil (dom/input #js{:type "checkbox"}))))))

;;
;; Only used for normalization. TrendingTab gets the queries and has the render
;;
(defui GasQueryPanel
  static om/Ident
  (ident [this props]
    [:gas-query-panel/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     {:grid/gas-query-grid (om/get-query GasQueryGrid)}
     {:graph/trending-graph (om/get-query graph/TrendingGraph)}
     ]))

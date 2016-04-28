(ns om-alarming.query
  (:require [om-alarming.components.navigator :as navigator]
            [om.next :as om]
            [om-alarming.components.graphing :as graph]
            [om-alarming.components.grid :as grid]
            [om-alarming.components.general :as gen]
            [om-alarming.components.login-dialog :as dialog]))

;;
;; Need to have this because we are pre-normalizing the state
;;
(def non-union-part-of-root-query
  [{:app/sys-gases (om/get-query gen/SystemGas)}
   {:app/tubes (om/get-query gen/Location)}
   {:tube/real-gases (om/get-query grid/GridDataCell)}
   {:grid/gas-query-grid (om/get-query grid/GasQueryGrid)}
   {:grid/gas-query-panel (om/get-query grid/GasQueryPanel)}
   {:graph/lines (om/get-query graph/Line)}
   {:graph/trending-graph (om/get-query graph/TrendingGraph)}
   {:graph/navigator (om/get-query navigator/GraphNavigator)}
   {:graph/misc (om/get-query graph/Misc)}
   {:graph/plumb-line (om/get-query graph/PlumbLine)}
   {:graph/x-gas-details (om/get-query graph/RectTextTick)}
   {:app/login-info (om/get-query dialog/LoginDialog)}
   {:graph/translators (om/get-query graph/Translators)}
   ])


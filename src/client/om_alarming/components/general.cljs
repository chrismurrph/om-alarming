(ns om-alarming.components.general
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util.util :refer [class-names]]
            [om-alarming.business :as bus]))

;;
;; Need an Ident for db->query to work. These are just the gases themselves, so there might only be 4 of them
;;
(defui SystemGas
       static om/Ident
       (ident [this props]
              [:gas-of-system/by-id (:id props)])
       static om/IQuery
       (query [this]
              [:id :short-name :lowest :highest :long-name]))

(defui Location
       static om/Ident
       (ident [this props]
              [:tube/by-id (:id props)])
       static om/IQuery
       (query [this]
              [:id :tube-num :display-name]))

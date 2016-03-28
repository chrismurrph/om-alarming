(ns om-alarming.misc.help
  (:require [om-alarming.util.utils :as u]
            [cljs.pprint :refer [pprint]]))

(def norm-state (atom {:graph/selected-lines [[:line/by-id 100] [:line/by-id 101]],
                       :graph/lines
                                             [[:line/by-id 100]
                                              [:line/by-id 101]
                                              [:line/by-id 102]
                                              [:line/by-id 103]],
                       :line/by-id
                                             {100 {:id 100, :name "Methane"},
                                              101 {:id 101, :name "Oxygen"},
                                              102 {:id 102, :name "Carbon Dioxide"},
                                              103 {:id 103, :name "Carbon Monoxide"}}}))

(defn mutate [state want-to-select? id]
  (let [ident [:line/by-id id]]
    (if want-to-select?
      (swap! state update :graph/selected-lines (fn [st] (as-> st $
                                                               (u/probe "init" $)
                                                               (conj $ ident)
                                                               (u/probe "post conj" $))))
      (swap! state update :graph/selected-lines (fn [lines] (vec (remove #{ident} lines))))))
  (pprint @state))

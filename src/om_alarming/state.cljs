(ns om-alarming.state
  (:require [om-alarming.graph.mock-values :refer [pink green blue red]]))

(def initial-state
  {:app/selected-button {:id 3}
   :app/buttons
   [{:id          1
     :name        "Map"
     :description "Mine plan"
     :showing     true}
    {:id          2
     :name        "Trending"
     :description "Live data, Trending"
     :showing     true}
    {:id          3
     :name        "Thresholds"
     :description "Alarm Thresholds"
     :showing     true}
    {:id          4
     :name        "Reports"
     :description "Event Reports"
     :showing     true}
    {:id          5
     :name        "Automatic"
     :description "Automatic Tube Bundle"
     :showing     true}
    {:id          6
     :name        "Logs"
     :description "Warning Log"
     :showing     true}
    ]
   :graph/drop-info
   {:id            0                                        ;; Just in case React needs it (or avoid React using $null)
    :x             50
    :graph/lines      [{:id 1} {:id 2} {:id 3} {:id 4}]
    :current-label {:name "Carbon Monoxide at 1" :dec-places 1}
    :graph/x-gas-details [{:id 1} {:id 2} {:id 3}]}
   :graph/x-gas-details
   [
    {:id 1 :name "Carbon Dioxide at 1", :proportional-y 146.33422462612975, :proportional-val 0.19667279430464207}
    {:id 2 :name "Carbon Monoxide at 1", :proportional-y 131.68775824757364, :proportional-val 11.337551649514731}
    {:id 3 :name "Oxygen at 1", :proportional-y 161.68775824757364, :proportional-val 10.337551649514731}
    ]
   :graph/lines
   [{:id     1
     :name "Methane at 1" :units "%" :colour pink
     :points [0.03 0.04 0.05 0.04 0.03 0.02 0.01 0.01 0.07 0.07 0.10 0.13]}
    {:id     2
     :name "Oxygen at 1" :units "%" :colour green
     :points [21.0 22.0 22.0 21.4 20.0 21.3 22.0 19.5 19.8 21.0 21.1 21.5]}
    {:id     3
     :name "Carbon Dioxide at 1" :units "%" :colour blue
     :points [0.05 0.08 0.07 0.09 0.10 0.20 0.21 0.23 0.27 0.13 0.18 0.19]}
    {:id     4
     :name "Carbon Monoxide at 1" :units "ppm" :colour red
     :points [7 8 9 10 11 12 11 10 9 8 7 6 5]}
    ]
   :app/gases [{:id 1 :gas :methane} {:id 2 :gas :oxygen} {:id 3 :gas :carbon-monoxide} {:id 4 :gas :carbon-dioxide}]
   :graph/current-label
   {:id 0 :name "Carbon Monoxide at 1" :dec-places 1}
   :app/tubes
   [{:id    1
     :tube/gases [{:id 1}
             {:id 2}
             {:id 3}
             {:id 4}
             ]}
    {:id    2
     :tube/gases [{:id 5}
             {:id 6}
             {:id 7}
             {:id 8}
             ]}
    {:id    3
     :tube/gases [{:id 9}
             {:id 10}
             {:id 11}
             {:id 12}
             ]}
    {:id    4
     :tube/gases [{:id 13}
             {:id 14}
             {:id 15}
             {:id 16}
             ]}
    {:id    5
     :tube/gases [{:id 17}
             {:id 18}
             {:id 19}
             {:id 20}
             ]}
    {:id    6
     :tube/gases [{:id 21}
             {:id 22}
             {:id 23}
             {:id 24}
             ]}
    {:id    7
     :tube/gases [{:id 25}
             {:id 26}
             {:id 27}
             {:id 28}
             ]}
    {:id    8
     :tube/gases [{:id 29}
             {:id 30}
             {:id 31}
             {:id 32}
             ]}
    {:id    9
     :tube/gases [{:id 33}
             {:id 34}
             {:id 35}
             {:id 36}
             ]}
    {:id    10
     :tube/gases [{:id 37}
             {:id 38}
             {:id 39}
             {:id 40}
             ]}
    ]
   :tube/gases
   [{:id       1
     :gas      :methane
     :selected true}
    {:id  2
     :gas :oxygen}
    {:id  3
     :gas :carbon-monoxide}
    {:id  4
     :gas :carbon-dioxide}
    {:id  5
     :gas :methane}
    {:id  6
     :gas :oxygen}
    {:id  7
     :gas :carbon-monoxide}
    {:id  8
     :gas :carbon-dioxide}
    {:id  9
     :gas :methane}
    {:id  10
     :gas :oxygen}
    {:id  11
     :gas :carbon-monoxide}
    {:id       12
     :gas      :carbon-dioxide
     :selected true}
    {:id  13
     :gas :methane}
    {:id       14
     :gas      :oxygen
     :selected true}
    {:id  15
     :gas :carbon-monoxide}
    {:id  16
     :gas :carbon-dioxide}
    {:id  17
     :gas :methane}
    {:id  18
     :gas :oxygen}
    {:id  19
     :gas :carbon-monoxide}
    {:id  20
     :gas :carbon-dioxide}
    {:id  21
     :gas :methane}
    {:id  22
     :gas :oxygen}
    {:id  23
     :gas :carbon-monoxide}
    {:id  24
     :gas :carbon-dioxide}
    {:id  25
     :gas :methane}
    {:id  26
     :gas :oxygen}
    {:id  27
     :gas :carbon-monoxide}
    {:id  28
     :gas :carbon-dioxide}
    {:id  29
     :gas :methane}
    {:id  30
     :gas :oxygen}
    {:id  31
     :gas :carbon-monoxide}
    {:id  32
     :gas :carbon-dioxide}
    {:id  33
     :gas :methane}
    {:id  34
     :gas :oxygen}
    {:id  35
     :gas :carbon-monoxide}
    {:id  36
     :gas :carbon-dioxide}
    {:id  37
     :gas :methane}
    {:id  38
     :gas :oxygen}
    {:id  39
     :gas :carbon-monoxide}
    {:id  40
     :gas :carbon-dioxide}
    ]
   }
  )

(defn test-select-7 []
  (:app/selected-button (assoc-in initial-state [:app/selected-button :id] 7)))


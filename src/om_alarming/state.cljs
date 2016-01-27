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
   {:id            10200
    :x             50
    :graph/lines      [{:id 100} {:id 101} {:id 102} {:id 103}]
    :current-label {:name "Carbon Monoxide at 1" :dec-places 1}
    :graph/x-gas-details [{:id 10100} {:id 10101} {:id 10102}]}
   :graph/x-gas-details
   [
    {:id 10100 :name "Carbon Dioxide at 1", :proportional-y 146.33422462612975, :proportional-val 0.19667279430464207}
    {:id 10101 :name "Carbon Monoxide at 1", :proportional-y 131.68775824757364, :proportional-val 11.337551649514731}
    {:id 10102 :name "Oxygen at 1", :proportional-y 161.68775824757364, :proportional-val 10.337551649514731}
    ]
   :graph/lines
   [{:id     100
     :name "Methane at 1" :units "%" :colour pink
     :points [0.03 0.04 0.05 0.04 0.03 0.02 0.01 0.01 0.07 0.07 0.10 0.13]}
    {:id     101
     :name "Oxygen at 1" :units "%" :colour green
     :points [21.0 22.0 22.0 21.4 20.0 21.3 22.0 19.5 19.8 21.0 21.1 21.5]}
    {:id     102
     :name "Carbon Dioxide at 1" :units "%" :colour blue
     :points [0.05 0.08 0.07 0.09 0.10 0.20 0.21 0.23 0.27 0.13 0.18 0.19]}
    {:id     103
     :name "Carbon Monoxide at 1" :units "ppm" :colour red
     :points [7 8 9 10 11 12 11 10 9 8 7 6 5]}
    ]
   :app/gases [{:id 150 :gas :methane} {:id 151 :gas :oxygen} {:id 152 :gas :carbon-monoxide} {:id 153 :gas :carbon-dioxide}]
   :graph/current-label
   {:id 10000 :name "Carbon Monoxide at 1" :dec-places 1}
   :app/tubes
   [{:id    1000
     :tube/gases [{:id 500}
             {:id 501}
             {:id 502}
             {:id 503}
             ]}
    {:id    1001
     :tube/gases [{:id 504}
             {:id 505}
             {:id 506}
             {:id 507}
             ]}
    {:id    1002
     :tube/gases [{:id 508}
             {:id 509}
             {:id 510}
             {:id 511}
             ]}
    {:id    1003
     :tube/gases [{:id 512}
             {:id 513}
             {:id 514}
             {:id 515}
             ]}
    {:id    1004
     :tube/gases [{:id 516}
             {:id 517}
             {:id 518}
             {:id 519}
             ]}
    {:id    1005
     :tube/gases [{:id 520}
             {:id 521}
             {:id 522}
             {:id 523}
             ]}
    {:id    1006
     :tube/gases [{:id 524}
             {:id 525}
             {:id 526}
             {:id 527}
             ]}
    {:id    1007
     :tube/gases [{:id 528}
             {:id 529}
             {:id 530}
             {:id 531}
             ]}
    {:id    1008
     :tube/gases [{:id 532}
             {:id 533}
             {:id 534}
             {:id 535}
             ]}
    {:id    1009
     :tube/gases [{:id 536}
             {:id 537}
             {:id 538}
             {:id 539}
             ]}
    ]
   :tube/gases
   [{:id       500
     :gas      :methane
     :selected true}
    {:id  501
     :gas :oxygen}
    {:id  502
     :gas :carbon-monoxide}
    {:id  503
     :gas :carbon-dioxide}
    {:id  504
     :gas :methane}
    {:id  505
     :gas :oxygen}
    {:id  506
     :gas :carbon-monoxide}
    {:id  507
     :gas :carbon-dioxide}
    {:id  508
     :gas :methane}
    {:id  509
     :gas :oxygen}
    {:id  510
     :gas :carbon-monoxide}
    {:id       511
     :gas      :carbon-dioxide
     :selected true}
    {:id  512
     :gas :methane}
    {:id       513
     :gas      :oxygen
     :selected true}
    {:id  514
     :gas :carbon-monoxide}
    {:id  515
     :gas :carbon-dioxide}
    {:id  516
     :gas :methane}
    {:id  517
     :gas :oxygen}
    {:id  518
     :gas :carbon-monoxide}
    {:id  519
     :gas :carbon-dioxide}
    {:id  520
     :gas :methane}
    {:id  521
     :gas :oxygen}
    {:id  522
     :gas :carbon-monoxide}
    {:id  523
     :gas :carbon-dioxide}
    {:id  524
     :gas :methane}
    {:id  525
     :gas :oxygen}
    {:id  526
     :gas :carbon-monoxide}
    {:id  527
     :gas :carbon-dioxide}
    {:id  528
     :gas :methane}
    {:id  529
     :gas :oxygen}
    {:id  530
     :gas :carbon-monoxide}
    {:id  531
     :gas :carbon-dioxide}
    {:id  532
     :gas :methane}
    {:id  533
     :gas :oxygen}
    {:id  534
     :gas :carbon-monoxide}
    {:id  535
     :gas :carbon-dioxide}
    {:id  536
     :gas :methane}
    {:id  537
     :gas :oxygen}
    {:id  538
     :gas :carbon-monoxide}
    {:id  539
     :gas :carbon-dioxide}
    ]
   }
  )

(defn test-select-7 []
  (:app/selected-button (assoc-in initial-state [:app/selected-button :id] 7)))


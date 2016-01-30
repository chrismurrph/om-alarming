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
   :app/gases [{:id 150 :long-name "Methane" :short-name "CH\u2084"}
               {:id 151 :long-name "Oxygen" :short-name "O\u2082"}
               {:id 152 :long-name "Carbon Monoxide" :short-name "CO"}
               {:id 153 :long-name "Carbon Dioxide" :short-name "CO\u2082"}]
   :graph/init {:height 250
                :width 640}
   :graph/translators {:horiz nil :vert nil :point nil}
   :graph/current-label nil ; {:id 10000 :name "Carbon Monoxide at 1" :dec-places 1}
   :graph/in-sticky-time? false
   :graph/hover-pos nil
   :graph/last-mouse-moment nil
   :graph/labels-visible? false
   :graph/labels []
   :app/tubes
   [{:id    1000
     :tube-num 1
     :tube/gases [{:id 500}
             {:id 501}
             {:id 502}
             {:id 503}
             ]}
    {:id    1001
     :tube-num 2
     :tube/gases [{:id 504}
             {:id 505}
             {:id 506}
             {:id 507}
             ]}
    {:id    1002
     :tube-num 3
     :tube/gases [{:id 508}
             {:id 509}
             {:id 510}
             {:id 511}
             ]}
    {:id    1003
     :tube-num 4
     :tube/gases [{:id 512}
             {:id 513}
             {:id 514}
             {:id 515}
             ]}
    {:id    1004
     :tube-num 5
     :tube/gases [{:id 516}
             {:id 517}
             {:id 518}
             {:id 519}
             ]}
    {:id    1005
     :tube-num 6
     :tube/gases [{:id 520}
             {:id 521}
             {:id 522}
             {:id 523}
             ]}
    {:id    1006
     :tube-num 7
     :tube/gases [{:id 524}
             {:id 525}
             {:id 526}
             {:id 527}
             ]}
    {:id    1007
     :tube-num 8
     :tube/gases [{:id 528}
             {:id 529}
             {:id 530}
             {:id 531}
             ]}
    {:id    1008
     :tube-num 9
     :tube/gases [{:id 532}
             {:id 533}
             {:id 534}
             {:id 535}
             ]}
    {:id    1009
     :tube-num 10
     :tube/gases [{:id 536}
             {:id 537}
             {:id 538}
             {:id 539}
             ]}
    ]
   :tube/gases
   [{:id       500
     :system-gas      {:id 150}
     :tube {:id 1000}
     :selected true}
    {:id  501
     :system-gas {:id 151}
     :tube {:id 1000}}
    {:id  502
     :system-gas {:id 152}
     :tube {:id 1000}}
    {:id  503
     :system-gas {:id 153}
     :tube {:id 1000}}

    {:id  504
     :system-gas {:id 150}
     :tube {:id 1001}}
    {:id  505
     :system-gas {:id 151}
     :tube {:id 1001}}
    {:id  506
     :system-gas {:id 152}
     :tube {:id 1001}}
    {:id  507
     :system-gas {:id 153}
     :tube {:id 1001}}

    {:id  508
     :system-gas {:id 150}
     :tube {:id 1002}}
    {:id  509
     :system-gas {:id 151}
     :tube {:id 1002}}
    {:id  510
     :system-gas {:id 152}
     :tube {:id 1002}}
    {:id       511
     :system-gas      {:id 153}
     :tube {:id 1002}
     :selected true}

    {:id  512
     :system-gas {:id 150}
     :tube {:id 1003}}
    {:id       513
     :system-gas      {:id 151}
     :tube {:id 1003}
     :selected true}
    {:id  514
     :system-gas {:id 152}
     :tube {:id 1003}}
    {:id  515
     :system-gas {:id 153}
     :tube {:id 1003}}

    {:id  516
     :system-gas {:id 150}
     :tube {:id 1004}}
    {:id  517
     :system-gas {:id 151}
     :tube {:id 1004}}
    {:id  518
     :system-gas {:id 152}
     :tube {:id 1004}}
    {:id  519
     :system-gas {:id 153}
     :tube {:id 1004}}

    {:id  520
     :system-gas {:id 150}
     :tube {:id 1005}}
    {:id  521
     :system-gas {:id 151}
     :tube {:id 1005}}
    {:id  522
     :system-gas {:id 152}
     :tube {:id 1005}}
    {:id  523
     :system-gas {:id 153}
     :tube {:id 1005}}

    {:id  524
     :system-gas {:id 150}
     :tube {:id 1006}}
    {:id  525
     :system-gas {:id 151}
     :tube {:id 1006}}
    {:id  526
     :system-gas {:id 152}
     :tube {:id 1006}}
    {:id  527
     :system-gas {:id 153}
     :tube {:id 1006}}

    {:id  528
     :system-gas {:id 150}
     :tube {:id 1007}}
    {:id  529
     :system-gas {:id 151}
     :tube {:id 1007}}
    {:id  530
     :system-gas {:id 152}
     :tube {:id 1007}}
    {:id  531
     :system-gas {:id 153}
     :tube {:id 1007}}

    {:id  532
     :system-gas {:id 150}
     :tube {:id 1008}}
    {:id  533
     :system-gas {:id 151}
     :tube {:id 1008}}
    {:id  534
     :system-gas {:id 152}
     :tube {:id 1008}}
    {:id  535
     :system-gas {:id 153}
     :tube {:id 1008}}

    {:id  536
     :system-gas {:id 150}
     :tube {:id 1009}}
    {:id  537
     :system-gas {:id 151}
     :tube {:id 1009}}
    {:id  538
     :system-gas {:id 152}
     :tube {:id 1009}}
    {:id  539
     :system-gas {:id 153}
     :tube {:id 1009}}
    ]
   }
  )

(defn test-select-7 []
  (:app/selected-button (assoc-in initial-state [:app/selected-button :id] 7)))


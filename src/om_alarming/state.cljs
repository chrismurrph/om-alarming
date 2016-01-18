(ns om-alarming.state)

(def initial-state
  {:buttons
   [{:id          1
     :name        "Map"
     :description "Mine plan"
     :showing     true
     :selected    false}
    {:id          2
     :name        "Trending"
     :description "Live data, Trending"
     :showing     true
     :selected    false}
    {:id          3
     :name        "Thresholds"
     :description "Alarm Thresholds"
     :showing     true
     :selected    false}
    {:id          4
     :name        "Reports"
     :description "Event Reports"
     :showing     true
     :selected    false}
    {:id          5
     :name        "Automatic"
     :description "Automatic Tube Bundle"
     :showing     true
     :selected    false}
    {:id          6
     :name        "Logs"
     :description "Warning Log"
     :showing     true
     :selected    false}
    ]
   :tubes
   [{:id    1
     :gases [{:id  1
              :gas :methane
              :selected true}
             {:id  2
              :gas :oxygen}
             {:id  3
              :gas :carbon-monoxide}
             {:id  4
              :gas :carbon-dioxide}
             ]}
    {:id    2
     :gases [{:id  1
              :gas :methane}
             {:id  2
              :gas :oxygen}
             {:id  3
              :gas :carbon-monoxide}
             {:id  4
              :gas :carbon-dioxide}
             ]}
    {:id    3
     :gases [{:id  1
              :gas :methane}
             {:id  2
              :gas :oxygen}
             {:id  3
              :gas :carbon-monoxide}
             {:id  4
              :gas :carbon-dioxide
              :selected true}
             ]}
    {:id    4
     :gases [{:id  1
              :gas :methane}
             {:id  2
              :gas :oxygen
              :selected true}
             {:id  3
              :gas :carbon-monoxide}
             {:id  4
              :gas :carbon-dioxide}
             ]}
    {:id    5
     :gases [{:id  1
              :gas :methane}
             {:id  2
              :gas :oxygen}
             {:id  3
              :gas :carbon-monoxide}
             {:id  4
              :gas :carbon-dioxide}
             ]}
    {:id    6
     :gases [{:id  1
              :gas :methane}
             {:id  2
              :gas :oxygen}
             {:id  3
              :gas :carbon-monoxide}
             {:id  4
              :gas :carbon-dioxide}
             ]}
    {:id    7
     :gases [{:id  1
              :gas :methane}
             {:id  2
              :gas :oxygen}
             {:id  3
              :gas :carbon-monoxide}
             {:id  4
              :gas :carbon-dioxide}
             ]}
    {:id    8
     :gases [{:id  1
              :gas :methane}
             {:id  2
              :gas :oxygen}
             {:id  3
              :gas :carbon-monoxide}
             {:id  4
              :gas :carbon-dioxide}
             ]}
    {:id    9
     :gases [{:id  1
              :gas :methane}
             {:id  2
              :gas :oxygen}
             {:id  3
              :gas :carbon-monoxide}
             {:id  4
              :gas :carbon-dioxide}
             ]}
    {:id    10
     :gases [{:id  1
              :gas :methane}
             {:id  2
              :gas :oxygen}
             {:id  3
              :gas :carbon-monoxide}
             {:id  4
              :gas :carbon-dioxide}
             ]}
    ]
   }
  )


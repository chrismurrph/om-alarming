(ns om-alarming.state
  (:require
    [om-alarming.util.colours :refer [pink green blue red]]
    [cljs-time.core :as time]))

;(def goog-date "function (opt_year, opt_month, opt_date, opt_hours,")
(def irrelevant-keys #{:om.next/queries
                       :app/debug
                       :app/route
                       :app/trending
                       :app/automatic
                       :app/thresholds
                       :app/reports
                       :app/logs
                       :app/map
                       })
(def okay-val-maps #{[:r :g :b]
                     [:horiz-fn :vert-fn :point-fn]})
(def check-config {:excluded-keys              irrelevant-keys
                   :okay-value-maps            okay-val-maps
                   :by-id-kw                   "by-id"
                   ;:acceptable-table-value-fn? (fn [v] (= goog-date (subs (str (type v)) 0 (count goog-date))))
                   })

(def initial-state

  {
   :app/route [:app/map '_]
   :app/map {:id 10500
             :map/name "Map"
             :map/description "Mine Plan"}
   :app/trending {:id 10501
                  :trending/name "Trending"
                  :trending/description "Live data, Trending"}
   :app/thresholds {:id 10502
                    :thresholds/name "Thresholds"
                    :thresholds/description "Alarm Thresholds"}
   :app/reports {:id 10503
                 :reports/name        "Reports"
                 :reports/description "Event Reports"}
   :app/automatic {:id 10504
                   :automatic/name        "Automatic"
                   :automatic/description "Automatic Tube Bundle"}
   :app/logs {:id 10505
              :logs/name        "Logs"
              :logs/description "Warning Log"}
   :app/debug {:id 10506
               :debug/name        "Debug"
               :debug/description "Debug while developing"}

   :app/selected-button {:id 1}
   :app/buttons
   [{:id          1
     :name        "Map"
     :description "Mine plan"
     :showing?     true}
    {:id          2
     :name        "Trending"
     :description "Live data, Trending"
     :showing?     true}
    {:id          3
     :name        "Thresholds"
     :description "Alarm Thresholds"
     :showing?     true}
    {:id          4
     :name        "Reports"
     :description "Event Reports"
     :showing?     true}
    {:id          5
     :name        "Automatic"
     :description "Automatic Tube Bundle"
     :showing?     true}
    {:id          6
     :name        "Logs"
     :description "Warning Log"
     :showing?     true}
    {:id          7
     :name        "Debug"
     :description "Debug while developing"
     :showing?     true}
    ]

   :graph/trending-graph
   {:id 10300
    :width 640
    :height 250
    :graph/navigator {:id 10600}
    :graph/lines [{:id 100} {:id 101} {:id 102} {:id 103}]
    :labels-visible? false
    :graph/plumb-line {:id 10201}
    :graph/translators {:horiz-fn nil :vert-fn nil :point-fn nil}
    :graph/misc {:id 10400}
    :hover-pos nil
    :last-mouse-moment nil
    }

   :graph/navigator
   {:id 10600
    :end-time (time/now)
    :span-seconds (* 60 60)
    :receiving? false
    }

   :graph/misc
   {:id 10400}

   :graph/x-gas-details
   [
    {:id 10100 :graph/line {:id 102}}
    {:id 10101 :graph/line {:id 103}}
    {:id 10102 :graph/line {:id 101}}
    {:id 10103 :graph/line {:id 100}}
    ]
   :graph/plumb-line {:id 10201
                      :visible? true
                      :x-position nil
                      :in-sticky-time? false
                      :graph/current-line {:id 101}
                      :graph/x-gas-details [{:id 10100} {:id 10101} {:id 10102} {:id 10103}]}
   :graph/lines
   [
    {:id     100
     :colour pink
     :intersect {:grid-cell/id 500}
     :graph/points []}
    {:id     101
     :colour green
     :intersect {:grid-cell/id 501}
     :graph/points []}
    {:id     102
     :colour blue
     :intersect {:grid-cell/id 503}
     :graph/points []}
    {:id     103
     :colour red
     :intersect {:grid-cell/id 502}
     :graph/points []}
    ]
   :app/gases [{:id 150 :long-name "Methane" :short-name "CH\u2084"
                :lowest 0.25 :highest 1 :units "%"}
               {:id 151 :long-name "Oxygen" :short-name "O\u2082"
                :lowest 19 :highest 12 :units "%"}
               {:id 152 :long-name "Carbon Monoxide" :short-name "CO"
                :lowest 30 :highest 55 :units "ppm"}
               {:id 153 :long-name "Carbon Dioxide" :short-name "CO\u2082"
                :lowest 0.5 :highest 1.35 :units "%"}]
   :graph/points []
   :app/tubes
   [{:id    1000
     :tube-num 1
     :tube/gases [{:grid-cell/id 500}
             {:grid-cell/id 501}
             {:grid-cell/id 502}
             {:grid-cell/id 503}
             ]}
    {:id    1001
     :tube-num 2
     :tube/gases [{:grid-cell/id 504}
             {:grid-cell/id 505}
             {:grid-cell/id 506}
             {:grid-cell/id 507}
             ]}
    {:id    1002
     :tube-num 3
     :tube/gases [{:grid-cell/id 508}
             {:grid-cell/id 509}
             {:grid-cell/id 510}
             {:grid-cell/id 511}
             ]}
    {:id    1003
     :tube-num 4
     :tube/gases [{:grid-cell/id 512}
             {:grid-cell/id 513}
             {:grid-cell/id 514}
             {:grid-cell/id 515}
             ]}
    {:id    1004
     :tube-num 5
     :tube/gases [{:grid-cell/id 516}
             {:grid-cell/id 517}
             {:grid-cell/id 518}
             {:grid-cell/id 519}
             ]}
    {:id    1005
     :tube-num 6
     :tube/gases [{:grid-cell/id 520}
             {:grid-cell/id 521}
             {:grid-cell/id 522}
             {:grid-cell/id 523}
             ]}
    {:id    1006
     :tube-num 7
     :tube/gases [{:grid-cell/id 524}
             {:grid-cell/id 525}
             {:grid-cell/id 526}
             {:grid-cell/id 527}
             ]}
    {:id    1007
     :tube-num 8
     :tube/gases [{:grid-cell/id 528}
             {:grid-cell/id 529}
             {:grid-cell/id 530}
             {:grid-cell/id 531}
             ]}
    {:id    1008
     :tube-num 9
     :tube/gases [{:grid-cell/id 532}
             {:grid-cell/id 533}
             {:grid-cell/id 534}
             {:grid-cell/id 535}
             ]}
    {:id    1009
     :tube-num 10
     :tube/gases [{:grid-cell/id 536}
             {:grid-cell/id 537}
             {:grid-cell/id 538}
             {:grid-cell/id 539}
             ]}
    ]
   :tube/gases
   [{:grid-cell/id       500
     :system-gas {:id 150}
     :tube {:id 1000}}
    {:grid-cell/id  501
     :system-gas {:id 151}
     :tube {:id 1000}}
    {:grid-cell/id  502
     :system-gas {:id 152}
     :tube {:id 1000}}
    {:grid-cell/id  503
     :system-gas {:id 153}
     :tube {:id 1000}}

    {:grid-cell/id  504
     :system-gas {:id 150}
     :tube {:id 1001}}
    {:grid-cell/id  505
     :system-gas {:id 151}
     :tube {:id 1001}}
    {:grid-cell/id  506
     :system-gas {:id 152}
     :tube {:id 1001}}
    {:grid-cell/id  507
     :system-gas {:id 153}
     :tube {:id 1001}}

    {:grid-cell/id  508
     :system-gas {:id 150}
     :tube {:id 1002}}
    {:grid-cell/id  509
     :system-gas {:id 151}
     :tube {:id 1002}}
    {:grid-cell/id  510
     :system-gas {:id 152}
     :tube {:id 1002}}
    {:grid-cell/id       511
     :system-gas      {:id 153}
     :tube {:id 1002}}

    {:grid-cell/id  512
     :system-gas {:id 150}
     :tube {:id 1003}}
    {:grid-cell/id       513
     :system-gas      {:id 151}
     :tube {:id 1003}}
    {:grid-cell/id  514
     :system-gas {:id 152}
     :tube {:id 1003}}
    {:grid-cell/id  515
     :system-gas {:id 153}
     :tube {:id 1003}}

    {:grid-cell/id  516
     :system-gas {:id 150}
     :tube {:id 1004}}
    {:grid-cell/id  517
     :system-gas {:id 151}
     :tube {:id 1004}}
    {:grid-cell/id  518
     :system-gas {:id 152}
     :tube {:id 1004}}
    {:grid-cell/id  519
     :system-gas {:id 153}
     :tube {:id 1004}}

    {:grid-cell/id  520
     :system-gas {:id 150}
     :tube {:id 1005}}
    {:grid-cell/id  521
     :system-gas {:id 151}
     :tube {:id 1005}}
    {:grid-cell/id  522
     :system-gas {:id 152}
     :tube {:id 1005}}
    {:grid-cell/id  523
     :system-gas {:id 153}
     :tube {:id 1005}}

    {:grid-cell/id  524
     :system-gas {:id 150}
     :tube {:id 1006}}
    {:grid-cell/id  525
     :system-gas {:id 151}
     :tube {:id 1006}}
    {:grid-cell/id  526
     :system-gas {:id 152}
     :tube {:id 1006}}
    {:grid-cell/id  527
     :system-gas {:id 153}
     :tube {:id 1006}}

    {:grid-cell/id  528
     :system-gas {:id 150}
     :tube {:id 1007}}
    {:grid-cell/id  529
     :system-gas {:id 151}
     :tube {:id 1007}}
    {:grid-cell/id  530
     :system-gas {:id 152}
     :tube {:id 1007}}
    {:grid-cell/id  531
     :system-gas {:id 153}
     :tube {:id 1007}}

    {:grid-cell/id  532
     :system-gas {:id 150}
     :tube {:id 1008}}
    {:grid-cell/id  533
     :system-gas {:id 151}
     :tube {:id 1008}}
    {:grid-cell/id  534
     :system-gas {:id 152}
     :tube {:id 1008}}
    {:grid-cell/id  535
     :system-gas {:id 153}
     :tube {:id 1008}}

    {:grid-cell/id  536
     :system-gas {:id 150}
     :tube {:id 1009}}
    {:grid-cell/id  537
     :system-gas {:id 151}
     :tube {:id 1009}}
    {:grid-cell/id  538
     :system-gas {:id 152}
     :tube {:id 1009}}
    {:grid-cell/id  539
     :system-gas {:id 153}
     :tube {:id 1009}}
    ]
   }
  )

(defn test-select-7 []
  (:app/selected-button (assoc-in initial-state [:app/selected-button :id] 7)))


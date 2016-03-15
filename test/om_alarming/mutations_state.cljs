(ns om-alarming.mutations-state)

(def state
  (atom
    {:graph/misc           [:misc/by-id 10400],
     :tube/gases
                           [[:gas-at-location/by-id 500]
                            [:gas-at-location/by-id 501]
                            [:gas-at-location/by-id 502]
                            [:gas-at-location/by-id 503]
                            [:gas-at-location/by-id 504]
                            [:gas-at-location/by-id 505]
                            [:gas-at-location/by-id 506]
                            [:gas-at-location/by-id 507]
                            [:gas-at-location/by-id 508]
                            [:gas-at-location/by-id 509]
                            [:gas-at-location/by-id 510]
                            [:gas-at-location/by-id 511]
                            [:gas-at-location/by-id 512]
                            [:gas-at-location/by-id 513]
                            [:gas-at-location/by-id 514]
                            [:gas-at-location/by-id 515]
                            [:gas-at-location/by-id 516]
                            [:gas-at-location/by-id 517]
                            [:gas-at-location/by-id 518]
                            [:gas-at-location/by-id 519]
                            [:gas-at-location/by-id 520]
                            [:gas-at-location/by-id 521]
                            [:gas-at-location/by-id 522]
                            [:gas-at-location/by-id 523]
                            [:gas-at-location/by-id 524]
                            [:gas-at-location/by-id 525]
                            [:gas-at-location/by-id 526]
                            [:gas-at-location/by-id 527]
                            [:gas-at-location/by-id 528]
                            [:gas-at-location/by-id 529]
                            [:gas-at-location/by-id 530]
                            [:gas-at-location/by-id 531]
                            [:gas-at-location/by-id 532]
                            [:gas-at-location/by-id 533]
                            [:gas-at-location/by-id 534]
                            [:gas-at-location/by-id 535]
                            [:gas-at-location/by-id 536]
                            [:gas-at-location/by-id 537]
                            [:gas-at-location/by-id 538]
                            [:gas-at-location/by-id 539]],
     :app/buttons
                           [[:button/by-id 1]
                            [:button/by-id 2]
                            [:button/by-id 3]
                            [:button/by-id 4]
                            [:button/by-id 5]
                            [:button/by-id 6]
                            [:button/by-id 7]],
     :graph/navigator      [:navigator/by-id 10600],
     :app/tubes
                           [[:tube/by-id 1000]
                            [:tube/by-id 1001]
                            [:tube/by-id 1002]
                            [:tube/by-id 1003]
                            [:tube/by-id 1004]
                            [:tube/by-id 1005]
                            [:tube/by-id 1006]
                            [:tube/by-id 1007]
                            [:tube/by-id 1008]
                            [:tube/by-id 1009]],
     :app/gases
                           [[:gas-of-system/by-id 150]
                            [:gas-of-system/by-id 151]
                            [:gas-of-system/by-id 152]
                            [:gas-of-system/by-id 153]],
     :x-gas-detail/by-id
                           {10100 {:id 10100, :graph/line [:line/by-id 102]},
                            10101 {:id 10101, :graph/line [:line/by-id 103]},
                            10102 {:id 10102, :graph/line [:line/by-id 101]},
                            10103 {:id 10103, :graph/line [:line/by-id 100]}},
     :gas-at-location/by-id
                           {512
                            {:id         512,
                             :system-gas [:gas-of-system/by-id 150],
                             :tube       [:tube/by-id 1003]},
                            513
                            {:id         513,
                             :system-gas [:gas-of-system/by-id 151],
                             :tube       [:tube/by-id 1003]},
                            514
                            {:id         514,
                             :system-gas [:gas-of-system/by-id 152],
                             :tube       [:tube/by-id 1003]},
                            515
                            {:id         515,
                             :system-gas [:gas-of-system/by-id 153],
                             :tube       [:tube/by-id 1003]},
                            516
                            {:id         516,
                             :system-gas [:gas-of-system/by-id 150],
                             :tube       [:tube/by-id 1004]},
                            517
                            {:id         517,
                             :system-gas [:gas-of-system/by-id 151],
                             :tube       [:tube/by-id 1004]},
                            518
                            {:id         518,
                             :system-gas [:gas-of-system/by-id 152],
                             :tube       [:tube/by-id 1004]},
                            519
                            {:id         519,
                             :system-gas [:gas-of-system/by-id 153],
                             :tube       [:tube/by-id 1004]},
                            520
                            {:id         520,
                             :system-gas [:gas-of-system/by-id 150],
                             :tube       [:tube/by-id 1005]},
                            521
                            {:id         521,
                             :system-gas [:gas-of-system/by-id 151],
                             :tube       [:tube/by-id 1005]},
                            522
                            {:id         522,
                             :system-gas [:gas-of-system/by-id 152],
                             :tube       [:tube/by-id 1005]},
                            523
                            {:id         523,
                             :system-gas [:gas-of-system/by-id 153],
                             :tube       [:tube/by-id 1005]},
                            524
                            {:id         524,
                             :system-gas [:gas-of-system/by-id 150],
                             :tube       [:tube/by-id 1006]},
                            525
                            {:id         525,
                             :system-gas [:gas-of-system/by-id 151],
                             :tube       [:tube/by-id 1006]},
                            526
                            {:id         526,
                             :system-gas [:gas-of-system/by-id 152],
                             :tube       [:tube/by-id 1006]},
                            527
                            {:id         527,
                             :system-gas [:gas-of-system/by-id 153],
                             :tube       [:tube/by-id 1006]},
                            528
                            {:id         528,
                             :system-gas [:gas-of-system/by-id 150],
                             :tube       [:tube/by-id 1007]},
                            529
                            {:id         529,
                             :system-gas [:gas-of-system/by-id 151],
                             :tube       [:tube/by-id 1007]},
                            530
                            {:id         530,
                             :system-gas [:gas-of-system/by-id 152],
                             :tube       [:tube/by-id 1007]},
                            531
                            {:id         531,
                             :system-gas [:gas-of-system/by-id 153],
                             :tube       [:tube/by-id 1007]},
                            500
                            {:id         500,
                             :system-gas [:gas-of-system/by-id 150],
                             :tube       [:tube/by-id 1000]},
                            532
                            {:id         532,
                             :system-gas [:gas-of-system/by-id 150],
                             :tube       [:tube/by-id 1008]},
                            501
                            {:id         501,
                             :system-gas [:gas-of-system/by-id 151],
                             :tube       [:tube/by-id 1000]},
                            533
                            {:id         533,
                             :system-gas [:gas-of-system/by-id 151],
                             :tube       [:tube/by-id 1008]},
                            502
                            {:id         502,
                             :system-gas [:gas-of-system/by-id 152],
                             :tube       [:tube/by-id 1000]},
                            534
                            {:id         534,
                             :system-gas [:gas-of-system/by-id 152],
                             :tube       [:tube/by-id 1008]},
                            503
                            {:id         503,
                             :system-gas [:gas-of-system/by-id 153],
                             :tube       [:tube/by-id 1000]},
                            535
                            {:id         535,
                             :system-gas [:gas-of-system/by-id 153],
                             :tube       [:tube/by-id 1008]},
                            504
                            {:id         504,
                             :system-gas [:gas-of-system/by-id 150],
                             :tube       [:tube/by-id 1001]},
                            536
                            {:id         536,
                             :system-gas [:gas-of-system/by-id 150],
                             :tube       [:tube/by-id 1009]},
                            505
                            {:id         505,
                             :system-gas [:gas-of-system/by-id 151],
                             :tube       [:tube/by-id 1001]},
                            537
                            {:id         537,
                             :system-gas [:gas-of-system/by-id 151],
                             :tube       [:tube/by-id 1009]},
                            506
                            {:id         506,
                             :system-gas [:gas-of-system/by-id 152],
                             :tube       [:tube/by-id 1001]},
                            538
                            {:id         538,
                             :system-gas [:gas-of-system/by-id 152],
                             :tube       [:tube/by-id 1009]},
                            507
                            {:id         507,
                             :system-gas [:gas-of-system/by-id 153],
                             :tube       [:tube/by-id 1001]},
                            539
                            {:id         539,
                             :system-gas [:gas-of-system/by-id 153],
                             :tube       [:tube/by-id 1009]},
                            508
                            {:id         508,
                             :system-gas [:gas-of-system/by-id 150],
                             :tube       [:tube/by-id 1002]},
                            509
                            {:id         509,
                             :system-gas [:gas-of-system/by-id 151],
                             :tube       [:tube/by-id 1002]},
                            510
                            {:id         510,
                             :system-gas [:gas-of-system/by-id 152],
                             :tube       [:tube/by-id 1002]},
                            511
                            {:id         511,
                             :system-gas [:gas-of-system/by-id 153],
                             :tube       [:tube/by-id 1002]}},
     :graph/x-gas-details
                           [[:x-gas-detail/by-id 10100]
                            [:x-gas-detail/by-id 10101]
                            [:x-gas-detail/by-id 10102]
                            [:x-gas-detail/by-id 10103]],
     :app/debug
                           {:id                10506,
                            :debug/name        "Debug",
                            :debug/description "Debug while developing"},
     :navigator/by-id
                           {10600
                            {:id           10600,
                             :end-time     nil,
                             :span-seconds 3600}},
     :app/route            [:app/map '_],
     :line/by-id
                           {100
                            {:id           100,
                             :colour       {:r 255, :g 0, :b 255},
                             :intersect    [:gas-at-location/by-id 500],
                             :graph/points []},
                            101
                            {:id           101,
                             :colour       {:r 0, :g 102, :b 0},
                             :intersect    [:gas-at-location/by-id 501],
                             :graph/points []},
                            102
                            {:id           102,
                             :colour       {:r 0, :g 51, :b 102},
                             :intersect    [:gas-at-location/by-id 503],
                             :graph/points []},
                            103
                            {:id           103,
                             :colour       {:r 255, :g 0, :b 0},
                             :intersect    [:gas-at-location/by-id 502],
                             :graph/points []}},
     :app/trending
                           {:id                   10501,
                            :trending/name        "Trending",
                            :trending/description "Live data, Trending"},
     :button/by-id
                           {1 {:id 1, :name "Map", :description "Mine plan", :showing? true},
                            2
                              {:id          2,
                               :name        "Trending",
                               :description "Live data, Trending",
                               :showing?    true},
                            3
                              {:id          3,
                               :name        "Thresholds",
                               :description "Alarm Thresholds",
                               :showing?    true},
                            4
                              {:id          4,
                               :name        "Reports",
                               :description "Event Reports",
                               :showing?    true},
                            5
                              {:id          5,
                               :name        "Automatic",
                               :description "Automatic Tube Bundle",
                               :showing?    true},
                            6 {:id 6, :name "Logs", :description "Warning Log", :showing? true},
                            7
                              {:id          7,
                               :name        "Debug",
                               :description "Debug while developing",
                               :showing?    true}},
     :plumb-line/by-id
                           {10201
                            {:id                 10201,
                             :visible?           true,
                             :x-position         nil,
                             :in-sticky-time?    false,
                             :graph/current-line [:line/by-id 101],
                             :graph/x-gas-details
                                                 [[:x-gas-detail/by-id 10100]
                                                  [:x-gas-detail/by-id 10101]
                                                  [:x-gas-detail/by-id 10102]
                                                  [:x-gas-detail/by-id 10103]]}},
     :graph/trending-graph [:trending-graph/by-id 10300],
     :gas-of-system/by-id
                           {150
                            {:id         150,
                             :long-name  "Methane",
                             :short-name "CH₄",
                             :lowest     0.25,
                             :highest    1,
                             :units      "%"},
                            151
                            {:id         151,
                             :long-name  "Oxygen",
                             :short-name "O₂",
                             :lowest     19,
                             :highest    12,
                             :units      "%"},
                            152
                            {:id         152,
                             :long-name  "Carbon Monoxide",
                             :short-name "CO",
                             :lowest     30,
                             :highest    55,
                             :units      "ppm"},
                            153
                            {:id         153,
                             :long-name  "Carbon Dioxide",
                             :short-name "CO₂",
                             :lowest     0.5,
                             :highest    1.35,
                             :units      "%"}},
     :app/automatic
                           {:id                    10504,
                            :automatic/name        "Automatic",
                            :automatic/description "Automatic Tube Bundle"},
     :app/thresholds
                           {:id                     10502,
                            :thresholds/name        "Thresholds",
                            :thresholds/description "Alarm Thresholds"},
     :misc/by-id           {10400 {:id 10400, :comms nil, :inner-chan nil}},
     :app/reports
                           {:id                  10503,
                            :reports/name        "Reports",
                            :reports/description "Event Reports"},
     :graph/lines
                           [[:line/by-id 100]
                            [:line/by-id 101]
                            [:line/by-id 102]
                            [:line/by-id 103]],
     :app/logs
                           {:id 10505, :logs/name "Logs", :logs/description "Warning Log"},
     :app/selected-button  [:button/by-id 1],
     :trending-graph/by-id
                           {10300
                            {:graph/misc        [:misc/by-id 10400],
                             :graph/navigator   [:navigator/by-id 10600],
                             :width             640,
                             :last-mouse-moment nil,
                             :graph/translators {:horiz-fn nil, :vert-fn nil, :point-fn nil},
                             :labels-visible?   false,
                             :id                10300,
                             :receiving?        false,
                             :graph/lines
                                                [[:line/by-id 100]
                                                 [:line/by-id 101]
                                                 [:line/by-id 102]
                                                 [:line/by-id 103]],
                             :hover-pos         nil,
                             :graph/plumb-line  [:plumb-line/by-id 10201],
                             :height            250}},
     :graph/plumb-line     [:plumb-line/by-id 10201],
     :app/map              {:id 10500, :map/name "Map", :map/description "Mine Plan"},
     :graph/points         [],
     :tube/by-id
                           {1000
                            {:id       1000,
                             :tube-num 1,
                             :tube/gases
                                       [[:gas-at-location/by-id 500]
                                        [:gas-at-location/by-id 501]
                                        [:gas-at-location/by-id 502]
                                        [:gas-at-location/by-id 503]]},
                            1001
                            {:id       1001,
                             :tube-num 2,
                             :tube/gases
                                       [[:gas-at-location/by-id 504]
                                        [:gas-at-location/by-id 505]
                                        [:gas-at-location/by-id 506]
                                        [:gas-at-location/by-id 507]]},
                            1002
                            {:id       1002,
                             :tube-num 3,
                             :tube/gases
                                       [[:gas-at-location/by-id 508]
                                        [:gas-at-location/by-id 509]
                                        [:gas-at-location/by-id 510]
                                        [:gas-at-location/by-id 511]]},
                            1003
                            {:id       1003,
                             :tube-num 4,
                             :tube/gases
                                       [[:gas-at-location/by-id 512]
                                        [:gas-at-location/by-id 513]
                                        [:gas-at-location/by-id 514]
                                        [:gas-at-location/by-id 515]]},
                            1004
                            {:id       1004,
                             :tube-num 5,
                             :tube/gases
                                       [[:gas-at-location/by-id 516]
                                        [:gas-at-location/by-id 517]
                                        [:gas-at-location/by-id 518]
                                        [:gas-at-location/by-id 519]]},
                            1005
                            {:id       1005,
                             :tube-num 6,
                             :tube/gases
                                       [[:gas-at-location/by-id 520]
                                        [:gas-at-location/by-id 521]
                                        [:gas-at-location/by-id 522]
                                        [:gas-at-location/by-id 523]]},
                            1006
                            {:id       1006,
                             :tube-num 7,
                             :tube/gases
                                       [[:gas-at-location/by-id 524]
                                        [:gas-at-location/by-id 525]
                                        [:gas-at-location/by-id 526]
                                        [:gas-at-location/by-id 527]]},
                            1007
                            {:id       1007,
                             :tube-num 8,
                             :tube/gases
                                       [[:gas-at-location/by-id 528]
                                        [:gas-at-location/by-id 529]
                                        [:gas-at-location/by-id 530]
                                        [:gas-at-location/by-id 531]]},
                            1008
                            {:id       1008,
                             :tube-num 9,
                             :tube/gases
                                       [[:gas-at-location/by-id 532]
                                        [:gas-at-location/by-id 533]
                                        [:gas-at-location/by-id 534]
                                        [:gas-at-location/by-id 535]]},
                            1009
                            {:id       1009,
                             :tube-num 10,
                             :tube/gases
                                       [[:gas-at-location/by-id 536]
                                        [:gas-at-location/by-id 537]
                                        [:gas-at-location/by-id 538]
                                        [:gas-at-location/by-id 539]]}}}))

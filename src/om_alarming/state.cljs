(ns om-alarming.state)

(def initial-state
  {:buttons
   [{:id 1
     :name "Map"
     :description "Mine plan"
     :showing true
     :selected false}
    {:id 2
     :name "Trending"
     :description "Live data, Trending"
     :showing true
     :selected false}
    {:id 3
     :name "Thresholds"
     :description "Alarm Thresholds"
     :showing true
     :selected false}
    {:id 4
     :name "Reports"
     :description "Event Reports"
     :showing true
     :selected false}
    {:id 5
     :name "Automatic"
     :description "Automatic Tube Bundle"
     :showing true
     :selected false}
    {:id 6
     :name "Logs"
     :description "Warning Log"
     :showing true
     :selected false}
    ]})


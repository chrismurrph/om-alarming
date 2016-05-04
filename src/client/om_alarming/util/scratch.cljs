(ns om-alarming.util.scratch)

(def yep
  {:id                  10201,
   :visible?            true,
   :graph/x-gas-details [{:id 10100, :graph/line {:id 102, :colour {:r 0, :g 51, :b 102}, :intersect {:grid-cell/id 503, :tube {:id 1000, :tube-num 1}, :system-gas {:id 153, :short-name "CO₂", :best 0.5, :worst 1.35, :long-name "Carbon Dioxide"}}}} {:id 10101, :graph/line {:id 103, :colour {:r 255, :g 0, :b 0}, :intersect {:grid-cell/id 502, :tube {:id 1000, :tube-num 1}, :system-gas {:id 152, :short-name "CO", :best 30, :worst 55, :long-name "Carbon Monoxide"}}}} {:id 10102, :graph/line {:id 101, :colour {:r 0, :g 102, :b 0}, :intersect {:grid-cell/id 501, :tube {:id 1000, :tube-num 1}, :system-gas {:id 151, :short-name "O₂", :best 19, :worst 12, :long-name "Oxygen"}}}} {:id 10103, :graph/line {:id 100, :colour {:r 255, :g 0, :b 255}, :intersect {:grid-cell/id 500, :tube {:id 1000, :tube-num 1}, :system-gas {:id 150, :short-name "CH₄", :best 0.25, :worst 1, :long-name "Methane"}}}}],
   :graph/current-line  {:id 101, :colour {:r 0, :g 102, :b 0}, :intersect {:grid-cell/id 501, :tube {:id 1000, :tube-num 1}, :system-gas {:id 151, :short-name "O₂", :best 19, :worst 12, :long-name "Oxygen"}}},
   :width               640,
   :height              250})

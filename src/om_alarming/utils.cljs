(ns om-alarming.utils
  )

(defn bisect-vertical-between [[x0 y0 val0] [x1 y1 val1] x]
  (let [x-diff (- x1 x0)
        y-diff (- y1 y0)
        val-diff (- val1 val0)
        y-ratio (/ y-diff x-diff)
        val-ratio (/ val-diff x-diff)
        x-from-start (- x x0)
        y-res (+ y0 (* x-from-start y-ratio))
        val-res (+ val0 (* x-from-start val-ratio))]
    {:proportional-y y-res :proportional-val val-res}))

(defn round [n]
  (js/Math.round n))

(defn exp [x n]
  (reduce * (repeat n x)))

(def sqrt (.-sqrt js/Math))

(defn distance [[x1 y1] [x2 y2]]
  (let [x-delta-squared (exp (- x2 x1) 2)
        y-delta-squared (exp (- y2 y1) 2)
        sum-of-differences (+ x-delta-squared y-delta-squared)
        now-squared (sqrt sum-of-differences)]
    (round now-squared)))

(defn log [& txts]
  (.log js/console (apply str txts)))

(defn no-log [& txts]
  ())

(defn str-seq
  ([seq msg]
   (letfn [(lineify-seq [items]
                        (apply str (interpose "\n" items)))]
     (str "\n--------start--------\n"
          msg "\nCOUNT: " (count seq) "\n"
          (lineify-seq seq) "\n---------end---------")))
  ([seq]
   (str-seq nil seq)))

(defn pr-seq
  ([seq msg]
   (.log js/console (str-seq seq msg)))
  ([seq]
   (pr-seq nil seq)))

;;
;; from-world and to-world are maps of type {:min _ :max _}
;; These max and min are inclusive, so the exact middle when :min 0 and :max 10 is 5
;; Note that we need to do precision-scaling at the end, as there needs to be an exact
;; pixel location where to put circle on the graph
;;
(defn scale [from-world to-world from-val]
  (let [from-diff (- (:max from-world) (:min from-world))
        to-diff (- (:max to-world) (:min to-world))
        from-proportion (/ from-val from-diff)
        res (+ (:min to-world) (* to-diff from-proportion))
        rounded-res (round res)
        ;_ (log rounded-res " | " res)
        ]
    rounded-res))

(defn style [& info]
  {:style (.trim (apply str (map #(let [[kwd val] %]
                                   (str (name kwd) " " val "; "))
                                 (apply hash-map info))))})

(def asc compare)
(def desc #(compare %2 %1))

;
; Comes from here:
; https://groups.google.com/forum/#!msg/clojure/VVVa3TS15pU/9WrN_9Mfao4J
;
(defn compare-by [[k comp & more] x y]
  (let [result (comp (k x) (k y))]
    (if (and (zero? result) (seq more))
      (recur more x y)
      result)))

;;
;; Removing elements from the middle of a vector isn't something vectors are necessarily good at.
;; If you have to do this often, consider using a hash-map so you can use dissoc.
;;
(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defn abs [val]
  (if (neg? val)
    (* -1 val)
    val))


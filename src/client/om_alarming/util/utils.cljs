(ns ^:figwheel-always om-alarming.util.utils
  (:require [clojure.string :as str]
            [clojure.set :as cset]
            [cljs.core.async :as async
             :refer [chan close! timeout <! >! alts!]]
            [om.next :as om])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

;;
;; A question about the `om/computed` and `om/get-computed` mechanism: if you want to pass down to grandchildren or
;; deeper, at each level you need to take apart the computed bundle and reassemble it for further down-passing, right?
;; There is no mechanism for having scope that is global to all descendants of a certain component? I agree that the
;; latter would be a less functional way, but the `computed` / `get-computed` dance in intermediate components feels
;; tedious.
;;
;; Usage: `(let [f (forward-computed-factory this (om/factory OtherComponent))] (f props {:other-computed ...}))`
;;
(defn forward-computed-factory
  [this f]
  (let [c (om/get-computed this)]
    (fn [props & [computes]]
      (f (om/computed props
                      (cond-> c
                              computes
                              (merge computes)))))))

;;
;; To use you will still put messages onto the in channel. But grab them out from the channel that is
;; returned here. So you need to have a go block that is always taking from this returned channel.
;;
;; http://stackoverflow.com/questions/35663415/throttle-functions-with-core-async/35663515#35663515
(defn debounce [in ms]
  (let [out (chan)]
    (go-loop [last-val nil]
             (let [val (if (nil? last-val) (<! in) last-val)
                   timer (timeout ms)
                   [new-val ch] (alts! [in timer])]
               (condp = ch
                 timer (do (>! out val) (recur nil))
                 in (recur new-val))))
    out))

;;
;; This is more the real one, b/c one above is for core.async
;; USE: (debounce #(onChange (-> % .-target .-value)))
;;
(defn another-debounce
  ([f] (debounce f 1000))
  ([f timeout]
   (let [id (atom nil)]
     (fn [evt]
       (if (not (nil? @id))
         (js/clearTimeout @id))
       (reset! id (js/setTimeout
                    (partial f evt)
                    timeout))))))

;; http://sids.github.io/nerchuko/utils-api.html
(defn unselect-keys
  "Opposite of select-keys: returns a map containing only those
entries whose key is not in keys."
  [m keyseq]
  (select-keys m
               (cset/difference (set (keys m))
                                       keyseq)))

(defn bisect-vertical-between [[x0 y0 val0] [x1 y1 val1] x]
  (assert x)
  (assert x0)
  (assert y0)
  (assert val0)
  (assert x1)
  (assert y1)
  (assert val1)
  ;; This problem has come about because earlier we rounded mouse to an int
  ;(assert (not= x0 x1) (str "Divide by zero as equal: " x0 " " x1))
  (let [x-diff (- x1 x0)
        y-diff (- y1 y0)
        val-diff (- val1 val0)
        y-ratio (/ y-diff (if (not= 0 x-diff) x-diff 1))
        val-ratio (/ val-diff (if (not= 0 x-diff) x-diff 1))
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

(defn probe [msg obj]
  (println (str (str/upper-case msg) ":\n" obj))
  obj)

(defn probe-off [msg obj]
  obj)

;;
;; Just a println would be fine in a javascript environ. But this way can code exactly
;; same in both environments, and port the code across easily.
;;

(def ^:private log-chan (chan))

(go-loop []
  (when-let [v (<! log-chan)]
    (println v)
    (recur)))

(defn log [debug msg]
  (when debug (async/put! log-chan msg)))

(defn log-on [msg]
  (log true msg))

(defn log-off [msg])

(defn first-only [seq]
  ;(assert (<= 1 (count seq)))
  (assert (= nil (second seq))) ;; better perf to check this way than count way
  (first seq))

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
  (let [min-from (:min from-world)
        max-from (:max from-world)
        min-to (:min to-world)
        max-to (:max to-world)
        from-diff (- max-from min-from)
        to-diff (- max-to min-to)
        from-proportion (/ (- from-val min-from) from-diff)
        res (+ min-to (* to-diff from-proportion))
        rounded-res (round res)
        ;_ (log "FROM VAL:" from-val " | RES:" rounded-res " | " res " | F:" from-world " | T:" to-world)
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

(defn vec-remove-value
  "If you are using this then question why vect is not a set in the first place"
  [vect valu]
  (assert (vector? vect))
  (let [as-set (into #{} vect)
        _ (assert (= (count as-set) (count vect))
                  "Not intended for removing a value where it is not there or > 1 there")
        res (vec (remove #{valu} as-set))
        _ (println "Down to " (count res) " from " (count vect) " b/c removed " valu)
        ]
    res))

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


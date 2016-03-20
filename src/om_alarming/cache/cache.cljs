(ns ^:figwheel-always om-alarming.cache.cache
  (:require [om-alarming.util.utils :as u]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.mutations-state :as st]
            [om-alarming.cache.matching :as match]
            [om-alarming.cache.range :as rng]
            [cljs.pprint :as pp :refer [pprint]]
            ))

;;;;;;;;;;;;; RELOADABLE start
(enable-console-print!)
(defmulti read om/dispatch)
(defmulti mutate om/dispatch)
(def parser
  (om/parser {:read   read
              :mutate mutate}))
(def reconciler
  (om/reconciler {:normalize false
                  :state     st/state ;; <- any old, seemed to need sumfin
                  :parser    parser}))
(defui Root
       Object
       (render [_]))
;;;;;;;;;;;;; RELOADABLE end

;;
;; In this "Date Data Cache" we don't allow gaps anywhere. Ranges are always contiguous
;;

(def uniqkey (atom 0))
(defn gen-uid []
  (let [res (swap! uniqkey inc)]
    ;(u/log res)
    res))

;;
;; Descriptions of what will be coming back. A pool has a uid that is matched with what is returned. The other
;; end will obviously know about this.
;; A pool will exist for a range of up to 10 minutes. Thus frequent data updates.
;;
(def ten-mins (* 10 60 1000))
(def five-mins (/ ten-mins 2))
(def hold-max-duration ten-mins)
(def pool-of-holds (atom (sorted-map)))

;;
;; Will be a map you can lookup using :id, where each has {:id nil :start nil :end nil :cb nil}. Re-created for
;; a query id when a query comes from the user. Will be updated whenever the cb is called, because there will be
;; a smaller range yet to come.
;;
(def current-ranges (atom {}))

;;
;; ga is for 'general area'. This is the cache! For each id there's a sorted map where key is date range
;; [start end] and value is all the points in that range.
;;
(def ga (atom (sorted-map)))

;;
;; Way to implement will be to grab all the points for the id. As the points will be in order there will
;; be a more efficient way later on, but for now we will just filter those within the range.
;; (Have a sorted set within each id and ask on SO!)
;;
(defn query-from-ga
  "Returns what part of the query is already known by ga, i.e. is already satisfied by the cache"
  [id {:keys [start end]}]
  (assert (and id start end))
  {:id id
   :start  0
   :end    ten-mins
   :points [{:a :a}
            {:b :a}
            {:c :a}]})

(defn create-hold
  ([cb id {:keys [start end]}]
   (let [new-hold {:uid   (gen-uid)
                   :id    id
                   :start start
                   :end   end
                   :cb    cb ;; <- just not nice to have to print it
                   }]
     new-hold)))

;;
;; pool-of-holds is indexed by uid so vals to get them all and filter on :id and
;; select-keys to just :start and :end
;;
(defn range-in-pool-of-holds [id]
  (->> (vals @pool-of-holds)
       (filter #(= (:id %) id))
       (map #(select-keys % [:start :end]))
       ;(u/probe (str "selected " id))
       (rng/sum-ranges)))

(defn update-pool-of-holds-new-range [cb id old-st range]
    ;(println "Reduce fn for " range)
    (if (pos? (rng/range-dur range))
      (let [new-hold (create-hold cb id range)]
        (assoc old-st (:uid new-hold) new-hold))
      old-st))

;;
;; old-pool-state is the whole of the state of the atom
;;
(defn update-pool-of-holds [cb old-pool-state id ranges]
  (let [pool-updater (partial update-pool-of-holds-new-range cb id)]
    (reduce pool-updater old-pool-state ranges)))

;;
;; Take one big range and split it up into many (or none)
;;
(defn segment-into-ranges [rnge]
  (let [rng-sz (rng/range-dur rnge)
        rng-start (:start rnge)]
    (if (zero? rng-sz)
      []
      (if (<= rng-sz hold-max-duration)
        [rnge]
        (let [num-segs (quot rng-sz hold-max-duration)
              extra-seg-dur (rem rng-sz hold-max-duration)
              extra-seg (when (pos? extra-seg-dur) {:start (- (:end rnge) extra-seg-dur) :end (:end rnge)})
              rnges (map (fn [num] {:start (+ rng-start (* num hold-max-duration)) :end (+ rng-start (* (inc num) hold-max-duration))}) (range num-segs))]
          (conj rnges extra-seg))))))

(defn current-range-updater [old-st id {:keys [start end]} cb]
  (assoc old-st id {:id id :start start :end end :cb cb}))

(defn query
  "Client makes this call and should expect its cb to be called many times
  id is an Intersect ident, but it could be anything.
  cb will actually be invoked right away with anything that is in the cache (called general area inside here).
  Returned values will always be in order"
  [id want-date-range cb]
  (swap! current-ranges current-range-updater id want-date-range cb)
  (let [ga-res (query-from-ga id want-date-range)]
    (cb ga-res)
    (let [pool-of-holds-updater (partial update-pool-of-holds cb)
          want-from-pool-range (some #(when (pos? (rng/range-dur %)) %) (rng/refine-need (select-keys ga-res [:start :end]) want-date-range))
          _ (println "WANT " want-from-pool-range)
          ;_ (assert (< (count want-ranges) 2))
          existing-pool-range (range-in-pool-of-holds id)
          _ (println "Need to take away: " existing-pool-range)
          want-after-seen-pool (filter (comp not #(rng/range-blank? %)) (rng/refine-need existing-pool-range want-from-pool-range))
          _ (assert (< (count want-after-seen-pool) 2) (str "Should never see split ranges: " want-after-seen-pool ", count: " (count want-after-seen-pool)))
          _ (println "NOW WANT " want-after-seen-pool)
          ]
      (swap! pool-of-holds pool-of-holds-updater id (mapcat segment-into-ranges want-after-seen-pool)))))

(defn run []
  (om/add-root! reconciler
                Root
                (.. js/document (getElementById "main-app-area")))
  (let [start-time 0
        duration (* hold-max-duration 10)
        bigger-dur (* hold-max-duration 10.5)
        smaller-dur (* hold-max-duration 9.5)]
    (query 1 {:start start-time :end (+ start-time smaller-dur)} (fn [res] (println "FIRST" res)))
    (query 1 {:start start-time :end (+ start-time duration)} (fn [res] (println "SECOND" res)))
    ;(pprint @pool-of-holds)
    ;(pprint @current-ranges)
    ;(println (vals @pool-of-holds))
    (println "Total range in pool of holds: " (range-in-pool-of-holds 1))
    )
  #_(let [want-range {:start 4 :end 8}
        existing-range {:start 3 :end 7}]
    (println "Wanting " want-range " and able to steal from " existing-range ", we covet: " (match/covet want-range existing-range)))
  )

(run)

(ns ^:figwheel-always om-alarming.cache.cache
  (:require [om-alarming.util.utils :as u]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.mutations-state :as st]
            [om-alarming.cache.matching :as match]
            [om-alarming.cache.range :as rng]
            [cljs.pprint :as pp :refer [pprint]]
            [om-alarming.graph.incoming :as incoming]
            [cljs.core.async :as async
             :refer [<! >! chan close! put! timeout alts!]]
            )
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def the-id [:gas-at-location/by-id 500])

;;
;; Descriptions of what will be coming back (pending). A pool has a uid that is matched with what is returned.
;; A pool will exist for a range of up to 10 minutes. Thus frequent data updates.
;;
(def pool-of-holds (atom (sorted-map)))

;;
;; ga is for 'general area'. This is the actual cache. For each id there's a sorted map where key is date range
;; {:start nil :end nil} and value is all the points in that range.
;;
(defn cf [rng]
  (:start rng))

(def ga (atom {}))

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
;; These s/be printed out as a whole
;(println "holds in pool:" @pool-of-holds)
;(println "GA:" @ga)
;;(println (vals @pool-of-holds))
(defui Root
       Object
       (render [_]
         (dom/div nil
                  (dom/button #js {:onClick #(pprint (keys (get @ga the-id)))} "GA")
                  (dom/br nil)
                  (dom/button #js {:onClick #(pprint @ga)} "All GA")
                  (dom/br nil)
                  (dom/button #js {:onClick #(pprint (vals @pool-of-holds))} "Holds"))))
;;;;;;;;;;;;; RELOADABLE end

;;
;; In this "Date Data Cache" we don't allow gaps anywhere. Ranges are always contiguous
;;

(def uniqkey (atom 0))
(defn gen-uid []
  (let [res (swap! uniqkey inc)]
    ;(u/log res)
    res))

(def ten-mins (* 10 60 1000))
(def five-mins (/ ten-mins 2))
(def hold-max-duration ten-mins)

;;
;; Will be a map you can lookup using :id, where each has {:id nil :start nil :end nil :cb nil}. Re-created for
;; a query id when a query comes from the user. Will be updated whenever the cb is called, because there will be
;; a smaller range yet to come.
;;
(def current-ranges (atom {}))

;;
;; Request results come back into this channel
;;
(def incoming-chan (chan))

;;
;; Way to implement will be to grab all the points for the id. As the points will be in order there will
;; be a more efficient way later on, but for now we will just filter those within the range.
;; (Have a sorted set within each id and ask on SO!)
;;
(defn query-from-ga
  "Returns what part of the query is already known by ga, i.e. is already satisfied by the cache"
  [id {:keys [start end]}]
  (assert (and id start end))
  (let [ranges (keys (get @ga the-id))
        betweens (filter (fn [rng]
                           (let [existing-start (:start rng)
                                 existing-end (:end rng)]
                             (and (> existing-start start) (< existing-end end)))) ranges)
        front-edge (some (fn [rng] (let [existing-start (:start rng)
                                         existing-end (:end rng)]
                                     (and (< existing-start start) (> existing-end start)))) ranges)
        back-edge (some (fn [rng] (let [existing-start (:start rng)
                                        existing-end (:end rng)]
                                     (and (< existing-start end) (> existing-end end)))) ranges)
        ;_ (println "Betweens: " betweens)
        ;_ (println "Front edge: " front-edge)
        ;_ (println "Back edge: " back-edge)
        middle-range (rng/sum-ranges betweens)
        front-range {:start start :end (:end front-edge)}
        back-range {:start (:start back-edge) :end end}
        ;_ (println "middle-range: " middle-range)
        ;_ (println "front-range: " front-range)
        ;_ (println "back-range: " back-range)
        res (rng/sum-ranges [front-range middle-range back-range])
        ;_ (println "INSIDE ga:" res)
        ]
    res)
  #_{:id id
   :start  nil #_start
   :end    nil #_end
   :points [{:a :a}
            {:b :a}
            {:c :a}]}
  #_(get-in @ga [id {:start start :end end}]))

;;
;; The id may not exist. We can't let assoc-in take care of any of this sort of thing as it will create a
;; normal hashmap (if anything - I don't 100% know). Thus if the id is not there we put in a map entry that
;; is [id (sorted-map-by cf)]
#_(defn create-sorted-map-by-cf [old-map]
  (if (nil? old-map)
    (let [new-map (sorted-map-by cf)]
      new-map)
    old-map))

;;
;; If there is nothing available to steal from the existing ranges then what we want
;; is not available, so accretion will be possible
;;
(defn accretion? [ranges start end]
  (let [sum-ranges (rng/sum-ranges ranges)
        want-to-steal {:start start :end end}
        _ (println "SUM of ranges is " sum-ranges "and we want to add:" want-to-steal)
        available (rng/covet want-to-steal sum-ranges)]
    (rng/range-blank? available)))

;;
;; Because this is a cache then incoming ranges should always be extra information. So we
;; check this and issue a warning.
;;
(defn update-ga [old-st id start end values]
  (let [has-id-st (if (= nil (get old-st id))
                    (assoc old-st id (sorted-map-by cf))
                    old-st)
        bad-problem (not (accretion? (keys (get has-id-st id)) start end))]
    ;(println has-id-st)
    #_(-> has-id-st
          (assoc-in id create-sorted-map-by-cf))
    (if bad-problem
      (do
        (println "WARNING: Not depositing into cache because always expect to be accreting into it")
        has-id-st)
      (-> has-id-st
          (assoc-in [id {:start start :end end}] values)))))

(defn deposit-into-ga [start end id values]
  ;(assert (nil? (get-in @ga [id {:start start :end end}])) (str "Don't expect the cache to already have:" start end id))
  (swap! ga update-ga id start end values))

(defn create-hold
  ([id {:keys [start end]}]
   (let [new-hold {:uid   (gen-uid)
                   :id    id
                   :start start
                   :end   end
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

(defn update-pool-of-holds-new-range [id old-st {:keys [start end] :as rnge}]
    ;(println "Reduce fn for " range)
    (if (pos? (rng/range-dur rnge))
      (let [new-hold (create-hold id rnge)
            gened-uid (:uid new-hold)
            _ (incoming/batch-generator start end {:id id :uid gened-uid} incoming-chan)]
        (assoc old-st gened-uid new-hold))
      old-st))

;;
;; old-pool-state is the whole of the state of the atom
;; Ranges can be in any order so need to sort them.
;;
(defn update-pool-of-holds [old-pool-state id ranges]
  ;(println "Ranges: " (sort-by cf ranges))
  (let [pool-updater (partial update-pool-of-holds-new-range id)]
    (reduce pool-updater old-pool-state (sort-by cf ranges))))

(defn discard-hold [old-pool-state uid]
  (-> old-pool-state
      (dissoc uid)))

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
    (let [want-from-pool-range (some #(when (pos? (rng/range-dur %)) %) (rng/refine-need (select-keys ga-res [:start :end]) want-date-range))
          _ (println "WANT " want-from-pool-range)
          ;_ (assert (< (count want-ranges) 2))
          existing-pool-range (range-in-pool-of-holds id)
          _ (println "Need to take away: " existing-pool-range)
          want-after-seen-pool (filter (comp not #(rng/range-blank? %)) (rng/refine-need existing-pool-range want-from-pool-range))
          _ (assert (< (count want-after-seen-pool) 2) (str "Should never see split ranges: " want-after-seen-pool ", count: " (count want-after-seen-pool)))
          _ (println "NOW WANT " want-after-seen-pool)
          ]
      (swap! pool-of-holds update-pool-of-holds id (remove #(rng/range-blank? %) (mapcat segment-into-ranges want-after-seen-pool))))))

;;
;; I haven't done any thinking about edges - that will come last
;;
(defn within-range [current-range start end]
  (let [acceptable-start (:start current-range)
        acceptable-end (:end current-range)]
    (and (>= start acceptable-start) (<= end acceptable-end))))

(defn batch-receipt [{:keys [info vals]}]
  (let [{:keys [id uid]} info
        hold (get @pool-of-holds uid)
        hold-start (:start hold)
        hold-end (:end hold)
        current-range (get @current-ranges id)
        user-want (select-keys current-range [:start :end])
        cb (:cb current-range)]
    (println "Received:" (count vals) "for" id uid)
    ;(println current-range)
    (println hold)
    (println)
    (deposit-into-ga hold-start hold-end id vals)
    (if (within-range user-want hold-start hold-end)
      (do
        (cb vals)
        (swap! pool-of-holds discard-hold uid))
      (do
        (println "WARNING: User's query has changed so need to go through the points and only deliver some, rest to ga")
        (println "user-want " user-want)
        (println "hold-start " hold-start)
        (println "hold-end " hold-end)))))

(defn make-receiver-component []
  (println "[receiver-component] starting")
  (let [poison-ch (chan)]
    (go-loop []
             (let [[v ch] (alts! [poison-ch incoming-chan])]
               (if (= ch poison-ch)
                 (println "[receiver-component] stopping")
                 (do
                   (batch-receipt v)
                   (recur)))))
    (fn stop! []
      (close! poison-ch))))

(defn run []
  (om/add-root! reconciler
                Root
                (.. js/document (getElementById "main-app-area")))
  (let [start-time 0
        duration (* hold-max-duration 10)
        bigger-dur (* hold-max-duration 10.5)
        smaller-dur (* hold-max-duration 9.5)]
    (make-receiver-component)
    (query the-id {:start start-time :end (+ start-time smaller-dur)} (fn [res] #_(println "FIRST" res)))
    (query the-id {:start start-time :end (+ start-time duration)} (fn [res] #_(println "SECOND" res)))
    ;;
    (println "Total range in pool of holds: " (range-in-pool-of-holds the-id))
    )
  #_(let [want-range {:start 4 :end 8}
        existing-range {:start 3 :end 7}]
    (println "Wanting " want-range " and able to steal from " existing-range ", we covet: " (match/covet want-range existing-range)))
  )

#_(run)

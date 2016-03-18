(ns ^:figwheel-always om-alarming.cache.cache
  (:require [om-alarming.util.utils :as u]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.mutations-state :as st]
            [om-alarming.cache.matching :as match]
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
(def pool-duration ten-mins)
(def pools (atom (sorted-map)))

#_(defn covers [repo-atom]
  (let [st @repo-atom
        beginning (-> st first val :start)
        ending (-> st last val :end)]
    {:start beginning :end ending}))

(defn same-pool [pool1 pool2]
  (when
    (and (= (:id pool1)) (= (:id pool2))
         (= (:start pool1)) (= (:start pool2))
         (= (:end pool1)) (= (:end pool2)))
    pool2))

(defn existing-pool [pool]
  (let [equiv-fn (partial same-pool pool)]
    (some equiv-fn (vals @pools))))

(defn overlap-pool [pool]
  (let [equiv-fn (partial match/overlap pool)]
    (some equiv-fn (vals @pools))))

;;
;; ga is for 'general area'. This is the cache! For each id there's a sorted map where key is date range
;; [start end] and value is all the points in that range.
;;
(def ga (atom (sorted-map)))

(defn from-ga
  [id {:keys [start end]}]
  (assert (and id start end))
  {:id id
   :start  0
   :end    ten-mins
   :points [{:a :a}
            {:b :a}
            {:c :a}]})

(defn range-dur [{:keys [start end]}]
  (- end start))

(defn create-pool
  ([cb id {:keys [start end]} needs-be-filled-by]
   (let [new-pool {:uid   (gen-uid)
                   :id    id
                   :start start
                   :end   end
                   ;:cb cb ;; <- just not nice to have to print it
                   }]
     (if (nil? needs-be-filled-by)
       new-pool
       (merge new-pool {:needs-be-filled-by needs-be-filled-by}))))
  ([cb id {:keys [start end] :as range}]
    (create-pool cb id range)))

;; new-pool (create-pool cb id range)
(defn update-pool [cb id old-st range]
  (if (pos? (range-dur range))
    (let [synthetic-pool {:id id :start (:start range) :end (:end range)}
          already-existing (existing-pool synthetic-pool)]
      (if (not already-existing)
        (let [already-overlapping (overlap-pool synthetic-pool)]
          (let [new-pool (create-pool cb id range (:uid already-overlapping))]
            (-> old-st
                (assoc (:uid new-pool) new-pool))))
        (-> old-st
            (update (:uid already-existing) merge {:cb cb}))))
    old-st))

(defn update-pools [cb old-pool id ranges]
  (let [pool-updater (partial update-pool cb id)]
    (reduce pool-updater old-pool ranges)))

;;
;; Take one big range and split it up into many (or none)
;;
(defn segment-into-ranges [rnge]
  (let [rng-sz (range-dur rnge)
        rng-start (:start rnge)]
    (if (zero? rng-sz)
      []
      (if (<= rng-sz pool-duration)
        [rnge]
        (let [num-segs (quot rng-sz pool-duration)
              extra-seg-dur (rem rng-sz pool-duration)
              extra-seg (when (pos? extra-seg-dur) {:start (- (:end rnge) extra-seg-dur) :end (:end rnge)})
              rnges (map (fn [num] {:start (+ rng-start (* num pool-duration)) :end (+ rng-start (* (inc num) pool-duration))}) (range num-segs))]
          (conj rnges extra-seg))))))

(defn query
  "Client makes this call and should expect its cb to be called many times
  id is an Intersect ident, but it could be anything.
  cb will actually be invoked right away with anything that is in the cache.
  Returned values will always be in order"
  [id want-date-range cb]
  (let [ga-res (from-ga id want-date-range)
        pools-updater (partial update-pools cb)]
    (cb ga-res)
    (let [ranges (match/refine-need (select-keys ga-res [:start :end]) want-date-range)]
      (when (some (fn [rng] (pos? (range-dur rng))) ranges)
        (swap! pools pools-updater id (mapcat segment-into-ranges ranges))))))

(defn run []
  (om/add-root! reconciler
                Root
                (.. js/document (getElementById "main-app-area")))
  (let [start-time 0
        duration (* pool-duration 10)]
    (query 1 {:start start-time :end (+ start-time duration)} (fn [res] (println "ONLY" res)))
    (pprint @pools)
    ))

(run)

(ns ^:figwheel-always om-alarming.cache
  (:require [om-alarming.util.utils :as u]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.mutations-state :as st]
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
;; In this "date data cache" we don't allow gaps anywhere. Ranges are always contiguous
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
(def pool (atom {}))

(defn create-pool [id {:keys [start end]}]
  {:uid (gen-uid)
   :id id
   :start start
   :end end})

;;
;; ga is for 'general area'. This is the cache! For each id there's a sorted map where key is date range
;; [start end] and value is all the points.
;;
(def ga (atom (sorted-map)))

(defn from-ga
  [id {:keys [start end]}]
  (assert (and id start end))
  {:id id
   :start  0
   :end    five-mins
   :points [{:a :a}
            {:b :a}
            {:c :a}]})

(defn range-satisfies
  [have-date-range want-date-range]
  "If have starts before (or at) and ends after (or at) want"
  (let [want-b4-have (- (:start have-date-range) (:start want-date-range))
        want-after-have (- (:end want-date-range) (:end have-date-range))
        satisfied-b4 (or (zero? want-b4-have) (neg? want-b4-have))
        satisfied-after (or (zero? want-after-have) (neg? want-after-have))]
    (println "RES: " want-b4-have want-after-have)
    {:satisfies?      (and satisfied-b4 satisfied-after)
     :want-b4-have    (when (not satisfied-b4) want-b4-have)
     :want-after-have (when (not satisfied-after) want-after-have)}))

(defn non-zero? [{:keys [start end]}]
  (pos? (- end start)))

(defn update-pool [old-pool id range]
  (if (non-zero? range)
    (let [new-pool (create-pool id range)]
      (-> old-pool
          (assoc (:uid new-pool) new-pool)))
    old-pool))

;; simpler than doing a reduce
(defn update-pools [old-pool id two-ranges]
  (-> old-pool
      (update-pool id (first two-ranges))
      (update-pool id (second two-ranges))))

(defn create-ranges [{:keys [start end]} {:keys [want-b4-have want-after-have]}]
  (let [before-range {:start start :end (+ start want-b4-have)}
        after-range {:start (- end want-after-have) :end end}
        both [before-range after-range]]
    ;(filter non-zero? both)
    both
    ))

(defn query
  "Client makes this call and should expect its cb to be called many times
  id is an Intersect ident, but it could be anything.
  cb will actually be invoked right away with anything that is in the cache.
  Returned values will always be in order"
  [id want-date-range cb]
  (let [ga-res (from-ga id want-date-range)
        ;{:keys [start end]} ga-res
        ]
    (cb ga-res)
    (let [satisfy-res (range-satisfies (select-keys ga-res [:start :end]) want-date-range)]
      (when (not (:satisfies? satisfy-res))
        (swap! pool update-pools id (create-ranges want-date-range satisfy-res))))))

(defn run []
  (om/add-root! reconciler
                Root
                (.. js/document (getElementById "main-app-area")))
  (query 1 {:start 0 :end ten-mins} (fn [res] (println "ONLY" res)))
  (pprint @pool))

(run)

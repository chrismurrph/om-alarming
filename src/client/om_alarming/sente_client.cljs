(ns om-alarming.sente-client
  (:require
    [clojure.string :as str]
    [cljs.core.async :as async :refer (<! >! put! chan)]
    [taoensso.encore :as encore :refer ()]
    [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
    [taoensso.sente :as sente :refer (cb-success?)]

    ;; Optional, for Transit encoding:
    [taoensso.sente.packers.transit :as sente-transit]
    [om.next :as om])

  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)]))

;; (timbre/set-level! :trace) ; Uncomment for more logging

;;;; Util for logging output to on-screen console

(def output-el (.getElementById js/document "output"))
(defn ->output! [fmt & args]
  (let [msg (apply encore/format fmt args)]
    (timbre/debug msg)
    (when output-el ;; just in case is not in the html
      (aset output-el "value" (str "• " (.-value output-el) "\n" msg))
      (aset output-el "scrollTop" (.-scrollHeight output-el)))))

(->output! "ClojureScript appears to have loaded correctly.")

;;;; Define our Sente channel socket (chsk) client

(let [;; For this example, select a random protocol:
      rand-chsk-type (if (>= (rand) 0.5) :ajax :auto)
      _ (->output! "Randomly selected chsk type: %s" rand-chsk-type)

      ;; Serializtion format, must use same val for client + server:
      packer :edn ; Default packer, a good choice in most cases
      ;; (sente-transit/get-flexi-packer :edn) ; Experimental, needs Transit dep

      {:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client!
        "/chsk" ; Must match server Ring routing URL
        {:type   rand-chsk-type
         :packer packer})]

  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

;;;; Sente event handlers

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  (->output! "Unhandled event: %s" event))

(defonce reconciler-atom_ (atom nil))
(defn rec []
  @reconciler-atom_)

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (if (and (:first-open? ?data) (:uid ?data) (not (= :taoensso.sente/nil-uid (:uid ?data))))
    (do
      (->output! "Channel socket successfully established with: %s" ?data)
      ;; `[(~mutate-key ~param-map)])
      (om/transact! (rec) '[(app/authenticate {:token true}) :app/login-info]))
    (->output! "Channel socket state change: %s" ?data)))

(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (->output! "Push event from server: %s" ?data))

(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (->output! "Handshake: %s" ?data)))

;; TODO Add your (defmethod -event-msg-handler <event-id> [ev-msg] <body>)s here...

;;;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! [reconciler]
  (stop-router!)
  (reset! router_
    (sente/start-client-chsk-router!
      ch-chsk event-msg-handler))
  (reset! reconciler-atom_
          reconciler))

(defn authentication? [ajax-resp]
  (let [okay? (:success? ajax-resp)]
    (->output! "Got back: %s in %s" okay? ajax-resp)
    okay?))

(defn login-process [user-id pass-id]
  (if (or (str/blank? user-id) (str/blank? pass-id))
    (js/alert "Please enter user-id and pass-id first")
    (do
      (->output! "Logging in with user-id %s" user-id)

      ;;; Use any login procedure you'd like. Here we'll trigger an Ajax
      ;;; POST request that resets our server-side session. Then we ask
      ;;; our channel socket to reconnect, thereby picking up the new
      ;;; session.

      (sente/ajax-lite "/login"
                       {:method :post
                        :headers {:X-CSRF-Token (:csrf-token @chsk-state)}
                        :params  {:user-id (str user-id) :pass-id (str pass-id)}}

                       (fn [ajax-resp]
                         (->output! "Ajax login response: %s" ajax-resp)
                         (let [login-successful? (authentication? ajax-resp)]
                           (if-not login-successful?
                             (do
                               (->output! "Login failed")
                               (om/transact! (rec) '[(app/authenticate {:token false})]))
                             (do
                               (->output! "Login successful, rec: " (rec))
                               (om/transact! (rec) '[(app/authenticate {:token true})])
                               (sente/chsk-reconnect! chsk)))))))))

#_(chsk-send! [:app/startup-info {}] (fn [cb-reply]
                                       (println "TZ Info: " cb-reply)))

(when-let [target-el (.getElementById js/document "btnlogout")]
  (.addEventListener target-el "click"
                     (fn [ev]
                       (->output! "Logout was clicked")
                       (om/transact! (rec) '[(app/authenticate {:token false} :app/login-info)])
                       (sente/ajax-lite "/logout"
                                        {:method :post
                                         :headers {:X-CSRF-Token (:csrf-token @chsk-state)}}
                                        (fn [ajax-resp]
                                          (->output! "Ajax logout response: %s" ajax-resp)
                                          (sente/chsk-reconnect! chsk))))))

(when-let [target-el (.getElementById js/document "btn-login-1")]
  (.addEventListener target-el "click"
                     (fn [ev]
                       (let [pass-id (.-value (.getElementById js/document "input-pass-login-1"))]
                         (login-process (.-name target-el) pass-id)))))

(when-let [target-el (.getElementById js/document "btn-login-2")]
  (.addEventListener target-el "click"
                     (fn [ev]
                       (let [user-id (.-value (.getElementById js/document "input-user-login-2"))
                             pass-id (.-value (.getElementById js/document "input-pass-login-2"))
                             ]
                         (login-process user-id pass-id)))))

;;;; Init stuff

(defn start! [reconciler] (start-router! reconciler))

;(defonce _start-once (start!))

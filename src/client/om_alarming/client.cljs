(ns om-alarming.client
  (:require
   [clojure.string  :as str]
   [cljs.core.async :as async  :refer (<! >! put! chan)]
   [taoensso.encore :as encore :refer ()]
   [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
   [taoensso.sente  :as sente  :refer (cb-success?)]

   ;; Optional, for Transit encoding:
   [taoensso.sente.packers.transit :as sente-transit])

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

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (if (= ?data {:first-open? true})
    (->output! "Channel socket successfully established!")
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
(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-client-chsk-router!
      ch-chsk event-msg-handler)))

;;;; UI events

(when-let [target-el (.getElementById js/document "btn1")]
  (.addEventListener target-el "click"
    (fn [ev]
      (->output! "Button 1 was clicked (won't receive any reply from server)")
      (chsk-send! [:example/button1 {:had-a-callback? "nope"}]))))

(when-let [target-el (.getElementById js/document "btn2")]
  (.addEventListener target-el "click"
    (fn [ev]
      (->output! "Button 2 was clicked (will receive reply from server)")
      (chsk-send! 
        [:example/points 
         {:start-time-str "01_03_2016__09_08_02.948"
          :end-time-str "07_03_2016__09_10_36.794"
          :metric-name "Oxygen"
          :display-name "Shed Tube 10"}] 5000
        (fn [cb-reply] (->output! "Callback reply: %s" cb-reply))))))

(defn authentication? [ajax-resp]
  (let [okay? (:success? ajax-resp)]
    (->output! "Got back: %s" okay?)
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
                             (->output! "Login failed")
                             (do
                               (->output! "Login successful")
                               (sente/chsk-reconnect! chsk)))))))))

(when-let [target-el (.getElementById js/document "btnlogout")]
  (.addEventListener target-el "click"
                     (fn [ev]
                       (->output! "Logout was clicked")
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

(defn start! [] (start-router!))

(defonce _start-once (start!))

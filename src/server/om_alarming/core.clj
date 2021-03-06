(ns om-alarming.core
  (:gen-class)

  (import (java.util Date HashMap)
          (com.cmts.server.business SmartgasServer GraphLineServer UserDetailsServer AuthenticationUserDetailsGetter)
          (com.seasoft.alarmer.common.domain DomainSession)
          (com.seasoft.common.utils Utils SeaLogger)
          (org.springframework.security.authentication UsernamePasswordAuthenticationToken)
          (com.seasoft.common.store ClientAuthenticationHolder)
          (org.springframework.security.core.userdetails UsernameNotFoundException)
          (com.cmts.common.service UserDetails)
          (com.cmts.server.objects.cayenne User))

  (:require
    [clojure.string :as str]
    [ring.middleware.defaults]
    [compojure.core :as comp :refer (defroutes GET POST)]
    [compojure.route :as route]
    [hiccup.core :as hiccup]
    [clojure.core.async :as async :refer (<! <!! >! >!! put! chan go go-loop)]
    [taoensso.encore :as encore :refer ()]
    [taoensso.timbre :as timbre :refer (tracef info infof warnf errorf)]
    [taoensso.sente :as sente]

    [org.httpkit.server :as http-kit]
    [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
    [om-alarming.util :as u]
    [om-alarming.convert :as conv]
    [clojure.java.io :as io]))

(defn start-selected-web-server! [ring-handler port]
  (infof "Starting http-kit...")
  (let [stop-fn (http-kit/run-server ring-handler {:port port})]
    {:server  nil ; http-kit doesn't expose this
     :port    (:local-port (meta stop-fn))
     :stop-fn (fn [] (stop-fn :timeout 100))}))

(let [packer :edn
      {:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket-server! sente-web-server-adapter
                                         {:packer packer})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(defn om-alarming-page-handler [ring-req]
  (hiccup/html
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/font-awesome/4.6.1/css/font-awesome.min.css" :type "text/css"}]
     [:link {:rel "stylesheet" :href "/css/base.css" :type "text/css"}]
     [:link {:rel "stylesheet" :href "/css/pure.css" :type "text/css"}]
     [:link {:rel "stylesheet" :href "/css/grids-responsive.css" :type "text/css"}]
     [:link {:rel "stylesheet" :href "/css/app.css" :type "text/css"}]
     [:script {:src "//d3js.org/d3.v3.min.js" :charset "utf-8"}]]
    [:body
     [:div {:id "main-app-area"}]
     [:script {:src "/js/main.js" :type "text/javascript"}]]))

;;;; Sente event handlers

(defmulti -event-msg-handler
          "Multimethod to handle Sente `event-msg`s"
          :id ; Dispatch on event-id
          )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (let [sg-sess (-> ev-msg :ring-req :session :uid)
        ;_ (infof "Asking to do %s FOR %s" id (-> ev-msg :ring-req :session :uid))
        ]
    (if sg-sess
      (-event-msg-handler ev-msg)
      {:status 404})))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (infof "Unhandled event: %s, id: %s" event id)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

(defmethod -event-msg-handler
  :chsk/ws-ping 
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    ;(debugf "Ignoring event: %s" event)
    ))

;; To debug from REPL - Cayenne is complaining yet these classes s/be available
(defn create-user []
  (let [example-user (User.)
        _ (.setFirstName example-user "Chris")
        _ (.setLastName example-user "Murphy")
        _ (.setStartTime example-user (Date.))]
    (info "Example user is: " example-user)
    (info "Started at: " (.getStartTime example-user))))

(defonce domain-factory_ (atom nil))
(defonce role-factory_ (atom nil))
(defonce smartgas-server_ (atom nil))
(defonce auth-server_ (atom nil))
(defonce graph-line-server_ (atom nil))
(defonce user-details-server_ (atom nil))
(defn start-smartgas-servers []
  ;; The component stuff was mis-configured. But a bad idea anyway - you can't componentize existing code.
  ;; Lets just not start SMARTGAS-connect if it is already running. We are not going to be altering the
  ;; existing server.
  (Utils/setupLoggingToFile "logs/smartgas")
  (when (= nil @domain-factory_)
    (reset! domain-factory_ (DomainSession/getDomainFactoryInstance))
    (reset! role-factory_ (DomainSession/getRoleEnumFactoryInstance))
    (reset! smartgas-server_ (SmartgasServer. (HashMap.)))
    (reset! auth-server_ (AuthenticationUserDetailsGetter. @smartgas-server_))
    (reset! graph-line-server_ (GraphLineServer. @smartgas-server_))
    (reset! user-details-server_ (UserDetailsServer. @smartgas-server_))))
#_(defn stop-smartgas-servers []
  (.discardState @smartgas-server_) ;; Method doesn't exist anymore - would be too difficult to make SMARTGAS reloadable
  (infof "Have discarded state on %s" @smartgas-server_))

(defn underscore [name]
  (clojure.string/replace name #" " "_"))

(defn get-points [?data session]
  (let [{:keys [start-time-str end-time-str metric-names display-name]} ?data
        multigasReqDO (.requestGraphLine @graph-line-server_ start-time-str end-time-str
                                         (map underscore metric-names)
                                         display-name (SeaLogger/format (Date.)) session)
        ]
    (map conv/points->vec (conv/multigas->outs multigasReqDO))))

;;
;; With this we are using Spring authentication the same way existing server code does. This
;; must be where seeing if the user is in the database. How to interrogate the db (user table
;; and its fields) must be specified in an XML file. Hmm - but there are no Spring XML files in
;; the uberjar, so must be in Java code somewhere. Well auth-server_ is an instance of a
;; subclass of a Spring security class, so that must be it. I have no idea the mechanism 403 gets
;; returned with, but it does. Just don't do this call and you will see it in the Chrome browser.
;;
(defn prevent-403 [user-id pass-id]
  (let [authObject (UsernamePasswordAuthenticationToken. user-id pass-id)
        authHolder (ClientAuthenticationHolder/getInstance)
        _ (.setServiceAuthentication authHolder authObject)]))

;;
;; Catch the exception and return nil if the user doesn't exist.
;;
(defn load-user [auth-server user-id]
  (assert auth-server)
  (assert user-id)
  (try (.loadUserByUsername auth-server user-id)
       (catch UsernameNotFoundException _ nil)))

(defn auth [session user-id pass-id]
  (let [_ (prevent-403 user-id pass-id)
        auth-res (load-user @auth-server_ user-id)
        authentic? (and auth-res (= pass-id (.getPassword auth-res)))
        res (if (not authentic?)
              {:status 404 :session (assoc session :uid nil)}
              (let [res (.getUserDetails (.getUserDetails @user-details-server_ user-id (.getRoleEnumCRO @role-factory_) (UserDetails/SMARTGAS_CLIENT_APP) false))
                    sg-sess (.getSessionId res)]
                {:status 200 :session (assoc session :uid sg-sess)}))
        _ (infof "AUTH RESPONSE: %s" res)]
    res))

(defmethod -event-msg-handler
  :graph/points
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [uid (get-in ring-req [:session :uid])]
    (infof "uid, ?data when points: %s, %s\n" uid ?data)
    (?reply-fn {:some-reply (get-points ?data uid)})))

(defmethod -event-msg-handler
  :app/startup-info
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (?reply-fn {:some-reply {:millis-advance-of-utc (u/millis-ahead-utc)}}))

(defn logout-handler
  [ring-req]
  {:status 200 :session (assoc (:session ring-req) :uid nil)})

;;
;; Logging in again is supposed to be idempotic - just keep returning true if already logged on.
;; But we do this simply by seeing if there's a :uid, so user giving a different password won't
;; be knocked out. Seems fair enough!
;;
(defn login-handler
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  [ring-req]
  (let [{:keys [session params]} ring-req
        {:keys [user-id pass-id]} params
        sg-sess (:uid session)
        _ (infof "Abt to authenticate %s, %s, SESS (only if this nil): %s" user-id pass-id sg-sess)
        auth-response (if sg-sess
                        {:status 200 :session session}
                        (auth session user-id pass-id))]
    auth-response))

;; TODO Add your (defmethod -event-msg-handler <event-id> [ev-msg] <body>)s here...

;;;; Sente event router (our `event-msg-handler` loop)

(defroutes ring-routes
           (GET  "/"      ring-req (om-alarming-page-handler      ring-req))
           (GET  "/index" ring-req (om-alarming-page-handler      ring-req))
           (GET  "/index.html" ring-req (om-alarming-page-handler      ring-req))
           (GET  "/chsk"  ring-req (ring-ajax-get-or-ws-handshake ring-req))
           (POST "/chsk"  ring-req (ring-ajax-post                ring-req))
           (POST "/login" ring-req (login-handler                 ring-req))
           (POST "/logout" ring-req (logout-handler               ring-req))
           (route/resources "/") ; Static files, notably public/main.js (our cljs target)
           (route/not-found "<h1>Page not found</h1>"))

(def main-ring-handler
  "**NB**: Sente requires the Ring `wrap-params` + `wrap-keyword-params`
  middleware to work. These are included with
  `ring.middleware.defaults/wrap-defaults` - but you'll need to ensure
  that they're included yourself if you're not using `wrap-defaults`."
  (-> ring-routes
      (ring.middleware.defaults/wrap-defaults ring.middleware.defaults/site-defaults)
      ))

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-server-chsk-router!
            ch-chsk event-msg-handler)))

(defonce    web-server_ (atom nil)) ; {:server _ :port _ :stop-fn (fn [])}
(defn  stop-web-server! [] (when-let [m @web-server_] ((:stop-fn m))))
(defn start-web-server! [& [port]]
  (stop-web-server!)
  (let [{:keys [stop-fn port] :as server-map}
        (start-selected-web-server! (var main-ring-handler)
                                    (or port 3000) ; 0 => auto (any available) port
                                    )
        uri (format "http://localhost:%s/" port)]
    (infof "Web server is running at `%s`" uri)
    ;(try
    ;  (.browse (java.awt.Desktop/getDesktop) (java.net.URI. uri))
    ;  (catch java.awt.HeadlessException _))
    (reset! web-server_ server-map)))

(defn stop!  []
  (stop-router!)
  (stop-web-server!)
  #_(stop-smartgas-servers))
(defn start! [] (start-router!) (start-web-server!)
  ;(create-user)
  (start-smartgas-servers))

;; (defonce _start-once (start!))

(defn -main "For `lein run`, etc." [] (start!))

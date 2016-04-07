(ns om-alarming.components.sente
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.sente-client :as client]
            [taoensso.sente :as sente]))

(defui Sente
       Object
       (render [this]
         (dom/div nil
                  (dom/h2 nil "Sente reference example")
                  (dom/p nil "An Ajax/WebSocket" (dom/strong nil " (random choice!)") " has been configured for this example")
                  (dom/p nil (dom/strong nil "Step 1") " try hitting the buttons:")
                  (dom/button #js{:type "button"
                                  :onClick (fn [_]
                                             (client/->output! "Button 2 was clicked (will receive reply from server)")
                                             (client/chsk-send!
                                               [:example/points
                                                {:start-time-str "01_03_2016__09_08_02.948"
                                                 :end-time-str "07_03_2016__09_10_36.794"
                                                 :metric-name "Oxygen"
                                                 :display-name "Shed Tube 10"}] 5000
                                               (fn [cb-reply] (client/->output! "Callback reply: %s" cb-reply))))} "chsk-send! (with reply)")
                  (dom/br nil)(dom/br nil)
                  (dom/p nil (dom/strong nil "Step 2") " observe std-out (for server output) and below (for client output):")
                  (dom/textarea #js{:style #js{:id "output" :width "100%" :height "200px"}})
                  ;(dom/hr nil)
                  (dom/h3 "Step 3: try login with a user-id")
                  (dom/p "The server can use this id to send events to *you* specifically.")
                  (dom/p nil
                         (dom/input #js{:type :text
                                        :placeholder "Pass-id"
                                        :id "input-pass-login-1"})
                         (dom/button #js{:type "button"
                                         :name "Chris"
                                         :id "btn-login-1"
                                         :onClick (fn [_]
                                                    (let [pass-id (.-value (.getElementById js/document "input-pass-login-1"))
                                                          _ (println "Pass retrieved: " pass-id)]
                                                      (client/login-process (.-name (.getElementById js/document "btn-login-1")) pass-id)))}
                                     "Chris login!"))
                  (dom/p nil
                         (dom/input #js{:type :text
                                        :placeholder "User-id"
                                        :id "input-user-login-2"})
                         (dom/input #js{:type :text
                                        :placeholder "Pass-id"
                                        :id "input-pass-login-2"})
                         (dom/button #js{:type "button"
                                         :id "btn-login-2"
                                         :onClick (fn [_]
                                                    (let [user-id (.-value (.getElementById js/document "input-user-login-2"))
                                                          pass-id (.-value (.getElementById js/document "input-pass-login-2"))
                                                          ]
                                                      (client/login-process user-id pass-id)))}
                                     "Secure login!"))
                  (dom/p nil
                         (dom/button #js{:type    "button"
                                         :onClick (fn [_]
                                                    (client/->output! "Logout was clicked")
                                                    (sente/ajax-lite "/logout"
                                                                     {:method  :post
                                                                      :headers {:X-CSRF-Token (:csrf-token @client/chsk-state)}}
                                                                     (fn [ajax-resp]
                                                                       (client/->output! "Ajax logout response: %s" ajax-resp)
                                                                       (sente/chsk-reconnect! client/chsk))))} "LOG OUT")))))
(def sente (om/factory Sente))

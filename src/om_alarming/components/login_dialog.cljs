(ns om-alarming.components.login-dialog
  (:require [goog.object :as gobj]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-alarming.util.utils :as u]

    ;[kanban.components.card :refer [Assignee assignee]]
    ;[kanban.components.sortable-list :refer [sortable-list]]
            ))

(defui LoginDialog
  static om/Ident
  (ident [this props]
    [:login-dlg/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:id
     :app/un
     :app/pw
     :app/authenticated?])
  Object
  (update [this prop value]
    (let [{:keys [update-fn]} (om/get-computed this)]
      (update-fn (u/probe-off "ident updating" (om/get-ident this)) {prop value})))
  (render [this]
    (let [{:keys [id app/name app/un app/pw]} (om/props this)
          {:keys [cancel-sign-in-fn sign-in-fn]} (om/get-computed this)]
      (dom/div #js {:className "dialog"}
               (dom/div #js {:className "dialog-closer" :onClick cancel-sign-in-fn})
               (dom/div #js {:className "dialog-content"}
                        (dom/h1 #js {:className "dialog-title"}
                                "Welcome" (dom/span #js {:className "board-name"} name))
                        (dom/form #js {:onSubmit #(.preventDefault %)}
                                  (dom/div #js {:className "form-row"}
                                           (dom/label nil "Name:")
                                           (dom/input
                                             #js {:value       un
                                                  :placeholder "Enter user name here..."
                                                  :onChange    #(.update this :app/un (.. % -target -value))}))
                                  (dom/div #js {:className "form-row"}
                                           (dom/label nil "Password:")
                                           (dom/input
                                             #js {:value       pw
                                                  :placeholder "Enter user password here..."
                                                  :onChange    #(.update this :app/pw (.. % -target -value))})))
                        (dom/p #js {:className "dialog-buttons"}
                               (dom/button #js{:onClick sign-in-fn} "Sign in")
                               (dom/button #js{:onClick cancel-sign-in-fn} "Cancel")))))))

(def login-dialog (om/factory LoginDialog {:keyfn :id}))

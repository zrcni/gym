(ns gym.views.login
  (:require
   [re-frame.core :refer [dispatch]]))

;; TODO: style
(defn login-view []
  (fn []
    [:button#login {:on-click #(dispatch [:login-auth0])} "Click to login"]))

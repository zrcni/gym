(ns gym.login.views
  (:require
   [gym.login.events :as events]
   [re-frame.core :refer [dispatch]]))

;; TODO: style
(defn main []
  (fn []
    [:button#login {:on-click #(dispatch [::events/login-auth0])} "Click to login"]))

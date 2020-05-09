(ns gym.login-callback.views
  (:require
   [gym.login.events]
   [re-frame.core :refer [dispatch]]))

(defn main []
  (dispatch [:gym.login.events/handle-login-auth0-callback])
  (fn [] [:div.circle-loader]))

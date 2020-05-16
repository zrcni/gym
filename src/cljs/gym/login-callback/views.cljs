(ns gym.login-callback.views
  (:require
   [gym.components.loaders :as loaders]
   [gym.login.events]
   [re-frame.core :refer [dispatch]]))

(defn main []
  (dispatch [:gym.login.events/handle-login-auth0-callback])
  (fn [] [loaders/circle]))

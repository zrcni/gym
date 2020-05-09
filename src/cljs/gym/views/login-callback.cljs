(ns gym.views.login-callback
  (:require
   [re-frame.core :refer [dispatch]]))

(defn login-callback-view []
  (dispatch [:handle-login-auth0-callback])
  (fn [] [:div.circle-loader]))

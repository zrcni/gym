(ns gym.login-callback.views
  (:require
   [gym.components.loaders :as loaders]
   [gym.login.events]
   [cljss.core :refer-macros [defstyles]]
   [re-frame.core :refer [dispatch]]))

(defstyles loader-wrapper-style []
  {:margin-top "1rem"
   :justify-content "center"
   :display "flex"
   :flex-direction "column"
   :align-items "center"})

(defn main []
  (dispatch [:gym.login.events/handle-login-auth0-callback])
  (fn []
    [:div {:class (loader-wrapper-style)}
     [loaders/circle {:size 80}]]))

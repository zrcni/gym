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

(defstyles loader-description-style []
  {:margin-top "1rem"
   :text-align "center"})

(defn main []
  ;; TODO: (wait and) check if logged in before dispatching
  (dispatch [::gym.login.events/handle-login-auth0-callback])
  (fn []
    [:div {:class (loader-wrapper-style)}
     [loaders/circle {:size 80}]
     [:p {:class (loader-description-style)}
      "The server might be starting right now, if you're the first user in a while..."]]))

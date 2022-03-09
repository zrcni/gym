(ns gym.frontend.login-callback.views
  (:require
   [gym.frontend.components.loaders :as loaders]
   [gym.frontend.login.events]
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
  (dispatch [::gym.frontend.login.events/handle-login-auth0-callback])
  (fn []
    [:div {:class (loader-wrapper-style)}
     [loaders/circle {:size 160}]]))

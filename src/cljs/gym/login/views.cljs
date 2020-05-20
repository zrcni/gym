(ns gym.login.views
  (:require
   [cljss.core :refer-macros [defstyles]]
   [gym.styles :as styles :refer [classes]]
   [gym.login.events :as events]
   [re-frame.core :refer [subscribe dispatch]]))

(defstyles login-wrapper-style []
  {:display "grid"
   :justify-content "center"
   :grid-template-column "auto"
   :grid-template-rows "auto auto"})

(defstyles login-button-wrapper-style []
  {:display "block"
   :margin "0 auto"})

(defstyles login-button-style []
  {:text-transform "uppercase"
   :font-weight 500})

(defstyles login-description-style []
  {:margin-top "0.5rem"
   "> *" {:max-width "25rem"
          :text-align "center"}})

(defn main []
  (let [theme @(subscribe [:theme])]
    [:div {:class (login-wrapper-style)}
     [:div {:class (login-button-wrapper-style)}
      [:button#login {:class (classes (styles/icon-button-cta {:theme theme}) (login-button-style))
                      :on-click #(dispatch [::events/login-auth0])}
       "Log in"]]
     [:div {:class (login-description-style)}
      [:p "A new user account will automatically be created for you when you log in for the first time"]]]))

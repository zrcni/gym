(ns gym.login.routes
  [:require
   [gym.login.views :as views]
   [gym.router-utils :refer [public-route]]])

(def routes
  ["login" {:name :login
            :view views/main
            :wrapper public-route
            :title "Login"
            :controllers []}]) 

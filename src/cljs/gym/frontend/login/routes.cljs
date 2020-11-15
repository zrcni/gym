(ns gym.frontend.login.routes
 (:require
  [gym.frontend.login.views :as views]
  [gym.frontend.router-utils :refer [public-route]]))

(def routes
  ["login" {:name :login
            :view views/main
            :wrapper public-route
            :title "Login"
            :controllers []}]) 

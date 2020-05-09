(ns gym.login-callback.routes
  (:require
   [gym.router-utils :refer [public-route]]
   [gym.login-callback.views :as views]))

(def routes
  ["auth0_callback" {:name :login-callback
                     :view views/main
                     :wrapper public-route
                     :title "Logging in..."
                     :controllers []}])

(ns gym.frontend.login-callback.routes
  (:require
   [gym.frontend.router-utils :refer [public-route]]
   [gym.frontend.login-callback.views :as views]))

(def routes
  ["auth0_callback" {:name :login-callback
                     :view views/main
                     :wrapper public-route
                     :title "Logging in..."
                     :controllers []}])

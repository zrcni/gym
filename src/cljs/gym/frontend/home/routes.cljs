(ns gym.frontend.home.routes
  (:require
   [gym.frontend.router-utils :refer [private-route]]
   [gym.frontend.home.views :as views]))

(def routes
  ["" {:name :home
       :view views/main
       :wrapper private-route
       :title "Home"
       :controllers []}])

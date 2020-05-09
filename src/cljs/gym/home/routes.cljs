(ns gym.home.routes
  (:require
   [gym.router-utils :refer [private-route]]
   [gym.home.views :as views]))

(def routes
  ["" {:name :home
       :view views/main
       :wrapper private-route
       :title "Home"
       :controllers []}])

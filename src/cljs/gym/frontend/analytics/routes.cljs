(ns gym.frontend.analytics.routes
  (:require
   [gym.frontend.router-utils :refer [private-route]]
   [gym.frontend.analytics.views :as views]))

(def routes
  ["analytics" {:name :analytics
                :view views/main
                :wrapper private-route
                :title "Analytics"
                :controllers []}])

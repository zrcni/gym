(ns gym.settings.routes
  (:require
   [gym.router-utils :refer [private-route]]
   [gym.settings.views :as views]))

(def routes
  ["settings" {:name :settings
               :view views/main
               :wrapper private-route
               :title "Settings"
               :controllers []}])

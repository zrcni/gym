(ns gym.frontend.settings.routes
  (:require
   [gym.frontend.router-utils :refer [private-route]]
   [gym.frontend.settings.views :as views]))

(def routes
  ["settings" {:name :settings
               :view views/main
               :wrapper private-route
               :title "Settings"
               :controllers []}])

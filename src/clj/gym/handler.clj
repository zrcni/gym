(ns gym.handler
  (:require
   [reitit.ring :as reitit-ring]
   [gym.middleware :refer [middleware]]
   [gym.web :refer [index-handler]]))

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    ["*" {:get {:handler index-handler}}])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))

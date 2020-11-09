(ns gym.handlers.web
  (:require
   [reitit.ring :as reitit-ring]
   [gym.middleware :refer [web-middlewares]]
   [gym.web :refer [index-handler]]))

(def handler
  (reitit-ring/ring-handler
   (reitit-ring/router
    ["*" {:get {:handler index-handler}}])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware web-middlewares}))

(ns gym.handler
  (:require
   [reitit.ring :as reitit-ring]
   [gym.middleware :refer [web-middlewares api-middlewares wrap-token]]
   [gym.workouts.route :refer [create-workouts-route]]
   [gym.users.route :refer [create-users-route]]
   [gym.auth.route :refer [create-auth-route]]
   [gym.web :refer [index-handler]]))

(def web-handler
  (reitit-ring/ring-handler
   (reitit-ring/router
    ["*" {:get {:handler index-handler}}])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware web-middlewares}))

(def api-handler
  (reitit-ring/ring-handler
   (reitit-ring/router
    ["/api"
     (create-workouts-route "/workouts")
     (create-users-route "/users")
     (create-auth-route "/auth")])
   (reitit-ring/routes
    (reitit-ring/create-default-handler))
   {:middleware (concat api-middlewares [wrap-token])}))

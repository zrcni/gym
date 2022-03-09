(ns gym.backend.routes
  (:require [clojure.java.io :as io]
            [reitit.ring :as reitit-ring]
            [gym.backend.middleware :refer [api-middlewares web-middlewares wrap-user wrap-prop]]
            [gym.backend.workouts.create-workout :as create-workout]
            [gym.backend.workouts.delete-workout-by-workout-id :as delete-workout-by-workout-id]
            [gym.backend.workouts.get-workout-by-workout-id :as get-workout-by-workout-id]
            [gym.backend.workouts.get-workouts-by-user-id :as get-workouts-by-user-id]
            [gym.backend.workouts.get-all-user-workout-tags :as get-all-user-workout-tags]
            [gym.backend.users.get-authenticated-user :as get-authenticated-user]
            [gym.backend.user-prefs.get-user-preferences :as get-user-preferences]
            [gym.backend.user-prefs.update-user-preferences :as update-user-preferences]
            [gym.backend.auth.login :as login]
            [gym.backend.analytics.analytics-query :as analytics-query]
            [gym.backend.config :as cfg]))

(defn index-handler [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body (-> (if cfg/dev? "public/html/index-dev.html" "public/html/index.html")
             (io/resource)
             (io/input-stream))})

(defn wrap [handler middleware]
  (middleware handler))

(defn create-routes [deps]
  (reitit-ring/ring-handler
   (reitit-ring/router
    ["/api"
     ["/analytics" {:get {:handler analytics-query/controller
                          :middleware [wrap-user]}}]


     ;; /workouts/tags route conflicts with /:workout-id
     ["/workout_tags" {:get {:handler get-all-user-workout-tags/controller
                             :middleware [wrap-user]}}]
     
     ["/workouts"
      ["" {:get {:handler get-workouts-by-user-id/controller
                 :middleware [wrap-user]}
           :post {:handler create-workout/controller
                  :middleware [wrap-user]}}]

      ["/:workout-id" {:get {:handler get-workout-by-workout-id/controller}
                       :delete {:handler delete-workout-by-workout-id/controller
                                :middleware [wrap-user]}}]]

     ["/users"
      ["/preferences" {:get {:handler get-user-preferences/controller
                             :middleware [wrap-user]}
                       :put {:handler update-user-preferences/controller
                             :middleware [wrap-user]}}]
      ["/token" {:get {:handler get-authenticated-user/controller}}]]

     ["/auth"
      ["/login" {:post {:handler login/controller}}]]]
    {:data {:middleware (conj api-middlewares (wrap-prop :deps deps))}})

   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    ;; NOTE: the normal and recommended way to apply middleware
    ;; with reitit is the way it's provided to the API router - in a data-driven fashion.
    ;; This is basically the only way to apply middleware specifically to the default handler.
    ;; AFAIK there's no way to have a match all route in addition to "/api", because "/api" conflicts with "*".
    ;; I don't want to add a proxy unless I need it for something else.
    (reduce wrap index-handler web-middlewares))))

(ns gym.backend.routes
  (:require [clojure.java.io :as io]
            [reitit.ring :as reitit-ring]
            [gym.backend.middleware :refer [api-middlewares web-middlewares wrap-user wrap-prop]]
            [gym.backend.workouts.create-workout :as create-workout]
            [gym.backend.workouts.delete-workout-by-workout-id :as delete-workout-by-workout-id]
            [gym.backend.workouts.get-workout-by-workout-id :as get-workout-by-workout-id]
            [gym.backend.workouts.get-workouts-by-user-id :as get-workouts-by-user-id]
            [gym.backend.users.get-authenticated-user :as get-authenticated-user]
            [gym.backend.auth.login :as login]
            [gym.backend.analytics.workout-duration-by-tag :as workout-duration-by-tag]
            [gym.backend.analytics.workouts-by-day-of-week :as workouts-by-day-of-week]
            [gym.backend.analytics.workouts-by-month-of-year :as workouts-by-month-of-year]
            [gym.backend.analytics.workout-duration-this-week :as workout-duration-this-week]
            [gym.backend.analytics.workout-duration-this-month :as workout-duration-this-month]
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
     ["/analytics"
      ["/workout_duration_by_tag" {:get {:handler workout-duration-by-tag/controller
                                         :middleware [wrap-user]}}]
      ["/workouts_by_day_of_week" {:get {:handler workouts-by-day-of-week/controller
                                         :middleware [wrap-user]}}]
      ["/workouts_by_month_of_year" {:get {:handler workouts-by-month-of-year/controller
                                           :middleware [wrap-user]}}]
      ["/workout_duration_this_week" {:get {:handler workout-duration-this-week/controller
                                            :middleware [wrap-user]}}]
      ["/workout_duration_this_month" {:get {:handler workout-duration-this-month/controller
                                             :middleware [wrap-user]}}]]
     
     ["/workouts"
      ["" {:get {:handler get-workouts-by-user-id/controller
                 :middleware [wrap-user]}
           :post {:handler create-workout/controller
                  :middleware [wrap-user]}}]

      ["/:workout-id" {:get {:handler get-workout-by-workout-id/controller}
                       :delete {:handler delete-workout-by-workout-id/controller
                                :middleware [wrap-user]}}]]

     ["/users/token" {:get {:handler get-authenticated-user/controller}}]

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

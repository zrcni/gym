(ns gym.handlers.api
  (:require
   [reitit.ring :as reitit-ring]
   [gym.middleware :refer [api-middlewares wrap-user]]
   [gym.workouts.controllers.core :as workouts-controllers]
   [gym.users.controllers.core :as users-controllers]
   [gym.auth.controllers.core :as auth-controllers]))

(def handler
  (reitit-ring/ring-handler
   (reitit-ring/router
    ["/api"
     ["/workouts"
      ["" {:get {:handler #'workouts-controllers/get-workouts-by-user-id
                 :middleware [wrap-user]}
           :post {:handler #'workouts-controllers/create-workout
                  :middleware [wrap-user]}}]

      ["/:workout-id" {:get {:handler #'workouts-controllers/get-workout-by-workout-id}
                       :delete {:handler #'workouts-controllers/delete-workout-by-workout-id
                                :middleware [wrap-user]}}]
      
      ["/duration"
       ["/week" {:get {:handler #'workouts-controllers/get-current-week-exercises-total-duration
                       :middleware [wrap-user]}}]
       ["/month" {:get {:handler #'workouts-controllers/get-current-month-exercises-total-duration
                        :middleware [wrap-user]}}]]]
     ["/users/token" {:get {:handler #'users-controllers/get-authenticated-user}}]

     ["/auth"
      ["/login" {:post {:handler #'auth-controllers/login}}]]])

   (reitit-ring/routes
    (reitit-ring/create-default-handler))
   {:middleware api-middlewares}))

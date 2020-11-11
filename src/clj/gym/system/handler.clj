(ns gym.system.handler
  (:require [integrant.core :as ig]
            [reitit.ring :as reitit-ring]
            [gym.middleware :refer [api-middlewares wrap-user]]
            [gym.workouts.controllers.core :as workouts-controllers]
            [gym.users.controllers.core :as users-controllers]
            [gym.auth.controllers.core :as auth-controllers]))

(defn create-handler [{:keys [user-repo
                              workout-repo
                              workout-duration-counter-weekly
                              workout-duration-counter-monthly]}]

  (reitit-ring/ring-handler
   (reitit-ring/router
    ["/api"
     ["/workouts"
      ["" {:get {:handler (workouts-controllers/get-workouts-by-user-id workout-repo)
                 :middleware [(wrap-user user-repo)]}
           :post {:handler (workouts-controllers/create-workout workout-repo)
                  :middleware [(wrap-user user-repo)]}}]

      ["/:workout-id" {:get {:handler (workouts-controllers/get-workout-by-workout-id workout-repo)}
                       :delete {:handler (workouts-controllers/delete-workout-by-workout-id workout-repo)
                                :middleware [(wrap-user user-repo)]}}]

      ["/duration"
       ["/week" {:get {:handler (workouts-controllers/get-current-week-exercises-total-duration workout-duration-counter-weekly)
                       :middleware [(wrap-user user-repo)]}}]
       ["/month" {:get {:handler (workouts-controllers/get-current-month-exercises-total-duration workout-duration-counter-monthly)
                        :middleware [(wrap-user user-repo)]}}]]]
     ["/users/token" {:get {:handler (users-controllers/get-authenticated-user user-repo)}}]

     ["/auth"
      ["/login" {:post {:handler (auth-controllers/login user-repo)}}]]])

   (reitit-ring/routes
    (reitit-ring/create-default-handler))
   {:middleware api-middlewares}))

(defmethod ig/init-key :system/handler [_ deps]
  (create-handler deps))

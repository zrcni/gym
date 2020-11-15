(ns gym.system.handler
  (:require [integrant.core :as ig]
            [reitit.ring :as reitit-ring]
            [gym.web :refer [index-handler]]
            [gym.middleware :refer [api-middlewares web-middlewares wrap-user]]
            [gym.workouts.controllers.core :as workouts-controllers]
            [gym.users.controllers.core :as users-controllers]
            [gym.auth.controllers.core :as auth-controllers]))

(defn wrap [handler middleware]
  (middleware handler))

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
      ["/login" {:post {:handler (auth-controllers/login user-repo)}}]]]
    {:data {:middleware api-middlewares}})
  
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    ;; NOTE: the normal and recommended way to apply middleware
    ;; with reitit is the way it's provided to the API router - in a data-driven fashion.
    ;; This is basically the only way to apply middleware specifically to the default handler.
    ;; AFAIK there's no way to have a match all route in addition to "/api", because "/api" conflicts with "*".
    ;; I don't want to add a proxy unless I need it for something else.
    (reduce wrap index-handler web-middlewares))))

(defmethod ig/init-key :system/handler [_ deps]
  (create-handler deps))

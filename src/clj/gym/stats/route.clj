(ns gym.stats.route
  (:require
   [gym.middleware :refer [wrap-user]]
   [gym.stats.counter :refer [current-week-exercise-durations
                              current-month-exercise-durations]]))

(defn get-current-week-exercise-durations [request]
  (let [user-id (-> request :context :user :user_id)]
    {:status 200
     :body {:duration ((-> current-week-exercise-durations :get) user-id)}
     :headers {"Content-Type" "application/json"}}))

(defn get-current-month-exercise-durations [request]
  (let [user-id (-> request :context :user :user_id)]
    {:status 200
     :body {:duration ((-> current-month-exercise-durations :get) user-id)}
     :headers {"Content-Type" "application/json"}}))

(defn create-stats-route [path]
  [path
   ["/exercises"
    ["/week" {:get {:handler get-current-week-exercise-durations
                              :middleware [wrap-user]}}]
    ["/month" {:get {:handler get-current-month-exercise-durations
                     :middleware [wrap-user]}}]]])

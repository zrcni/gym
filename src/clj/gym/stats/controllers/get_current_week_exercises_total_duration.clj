(ns gym.stats.controllers.get-current-week-exercises-total-duration
  (:require [gym.stats.counters.workout-duration-counter :refer [get-duration]]))

(defn create [weekly-workout-duration-counter]
  (fn [req]
    (let [user-id (get-in req [:context :user :user_id])
          duration (get-duration weekly-workout-duration-counter user-id)]

      {:status 200
       :body {:duration duration}
       :headers {"Content-Type" "application/json"}})))

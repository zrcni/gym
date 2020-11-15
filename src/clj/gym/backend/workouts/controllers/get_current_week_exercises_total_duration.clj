(ns gym.backend.workouts.controllers.get-current-week-exercises-total-duration
  (:require [gym.backend.workouts.counters.weekly-counter :refer [get-count]]
            [gym.backend.date-utils :refer [local-date get-week-of-year get-year]]))

;; TODO: ?week=48&year=2020
(defn create [weekly-workout-duration-counter]
  (fn [req]
    (let [user-id (get-in req [:context :user :user_id])
          date (local-date)
          week (get-week-of-year date)
          year (get-year date)
          duration (get-count weekly-workout-duration-counter week year user-id)]

      {:status 200
       :body {:duration duration}
       :headers {"Content-Type" "application/json"}})))

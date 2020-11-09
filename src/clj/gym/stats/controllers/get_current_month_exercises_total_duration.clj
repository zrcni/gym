(ns gym.stats.controllers.get-current-month-exercises-total-duration
  (:require [gym.stats.counters.monthly-counter :refer [get-count]]
            [gym.date-utils :refer [local-date get-month get-year]]))

;; TODO: ?week=48&year=2020
(defn create [monthly-workout-duration-counter]
  (fn [req]
    (let [user-id (get-in req [:context :user :user_id])
          date (local-date)
          month (get-month date)
          year (get-year date)
          duration (get-count monthly-workout-duration-counter month year user-id)]

      {:status 200
       :body {:duration duration}
       :headers {"Content-Type" "application/json"}})))

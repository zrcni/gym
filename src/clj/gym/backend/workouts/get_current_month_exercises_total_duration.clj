(ns gym.backend.workouts.get-current-month-exercises-total-duration
  (:require [gym.backend.workouts.counters.monthly-counter :refer [get-count]]
            [gym.backend.date-utils :refer [local-date get-month get-year]]))

;; TODO: ?week=48&year=2020
(defn controller [req]
  (let [{:keys [workout-duration-counter-monthly]} (-> req :deps)
        user-id (get-in req [:user :user_id])
        date (local-date)
        month (get-month date)
        year (get-year date)
        duration (get-count workout-duration-counter-monthly month year user-id)]

    {:status 200
     :body {:duration duration}
     :headers {"Content-Type" "application/json"}}))

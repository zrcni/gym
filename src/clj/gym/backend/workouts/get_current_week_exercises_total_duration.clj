(ns gym.backend.workouts.get-current-week-exercises-total-duration
  (:require [gym.backend.workouts.counters.weekly-counter :refer [get-count]]
            [gym.backend.date-utils :refer [local-date get-week-of-year get-year]]))

;; TODO: ?week=48&year=2020
(defn controller [req]
  (let [{:keys [workout-duration-counter-weekly]} (-> req :deps)
        user-id (get-in req [:user :user_id])
        date (local-date)
        week (get-week-of-year date)
        year (get-year date)
        duration (get-count workout-duration-counter-weekly week year user-id)]

    {:status 200
     :body {:duration duration}
     :headers {"Content-Type" "application/json"}}))

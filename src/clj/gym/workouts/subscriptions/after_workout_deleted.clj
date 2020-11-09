(ns gym.workouts.subscriptions.after-workout-deleted
  (:import java.time.LocalDate)
  (:require [gym.stats.counters.workout-duration-counter :refer [decrement-duration]]
            [gym.date-utils :refer [current-week? current-month?]]
            [gym.util :refer [ms->s]]))

(defn create [weekly-workout-duration-counter
              monthly-workout-duration-counter]
  (fn [event]
    (let [workout (-> event :payload :workout)
          duration-sec (ms->s (:duration workout))
          local-date (LocalDate/parse (:date workout))
          user_id (str (:user_id workout))]

      (when (current-week? local-date)
        (decrement-duration weekly-workout-duration-counter user_id duration-sec))
      (when (current-month? local-date)
        (decrement-duration monthly-workout-duration-counter user_id duration-sec)))))

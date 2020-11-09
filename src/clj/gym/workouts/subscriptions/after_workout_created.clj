(ns gym.workouts.subscriptions.after-workout-created
  (:import java.time.LocalDate)
  (:require [gym.stats.counters.workout-duration-counter :refer [increment-duration]]
            [gym.date-utils :refer [current-week? current-month?]]
            [gym.util :refer [ms->s]]))

(defn create [weekly-workout-duration-counter
              monthly-workout-duration-counter]
  (fn [event]
    (let [workout (-> event :payload :workout)
          duration-sec (ms->s (:duration workout))
          date (:date workout)
          user_id (str (:user_id workout))]

      (when (current-week? (LocalDate/parse date))
        (increment-duration weekly-workout-duration-counter user_id duration-sec))
      (when (current-month? (LocalDate/parse date))
        (increment-duration monthly-workout-duration-counter user_id duration-sec)))))

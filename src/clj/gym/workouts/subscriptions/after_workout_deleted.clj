(ns gym.workouts.subscriptions.after-workout-deleted
  (:require [gym.stats.counters.weekly-counter :as weekly-counter]
            [gym.stats.counters.monthly-counter :as monthly-counter]
            [gym.date-utils :refer [get-week-of-year get-year local-date get-month]]
            [gym.util :refer [ms->s]]))

(defn create [weekly-workout-duration-counter
              monthly-workout-duration-counter]
  (fn [event]
    (let [workout (-> event :payload :workout)
          duration-sec (ms->s (:duration workout))
          user_id (str (:user_id workout))
          date (local-date (:date workout))
          week (get-week-of-year date)
          month (get-month date)
          year (get-year date)]

      (weekly-counter/decr-count weekly-workout-duration-counter
                                 week year user_id duration-sec)
      
      (monthly-counter/decr-count monthly-workout-duration-counter
                                  month year user_id duration-sec))))

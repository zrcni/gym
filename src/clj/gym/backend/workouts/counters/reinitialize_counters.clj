(ns gym.backend.workouts.counters.reinitialize-counters
  (:require [taoensso.carmine :as car :refer (wcar)]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [gym.backend.workouts.counters.weekly-counter :as weekly-counter]
            [gym.backend.workouts.counters.monthly-counter :as monthly-counter]
            [gym.backend.date-utils :refer [get-week-of-year get-month get-year]]
            [gym.util :refer [ms->s]]))

(defn get-workout-durations [postgres]
  (sql/query postgres
             ["SELECT SUM(duration), date, user_id FROM workouts GROUP BY date, user_id"]
             {:builder-fn rs/as-unqualified-maps}))

(defn clear-counters [redis]
  (let [keys (wcar redis (car/keys "count:workout-duration*"))]
    (wcar redis (mapv car/del keys))))

(defn update-counters [workout-duration-counter-weekly
                       workout-duration-counter-monthly]
  (fn [result]
    (let [user-id (str (:user_id result))
          duration-sec (ms->s (:sum result))
          date (.toLocalDate (:date result))
          week (get-week-of-year date)
          month (get-month date)
          year (get-year date)]

      (weekly-counter/incr-count workout-duration-counter-weekly week year user-id duration-sec)
      (monthly-counter/incr-count workout-duration-counter-monthly month year user-id duration-sec))))

(defn exec [system]
  (let [postgres (:system/postgres system)
        redis (:system/redis system)
        workout-duration-counter-weekly (:system/workout-duration-counter-weekly system)
        workout-duration-counter-monthly (:system/workout-duration-counter-monthly system)
        update-fn (update-counters workout-duration-counter-weekly
                                   workout-duration-counter-monthly)]

    (clear-counters redis)
    (dorun (map update-fn (get-workout-durations postgres)))))

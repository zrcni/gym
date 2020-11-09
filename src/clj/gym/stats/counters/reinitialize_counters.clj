(ns gym.stats.counters.reinitialize-counters
  (:require [gym.stats.counters.core :refer [weekly-workout-duration-counter
                                             monthly-workout-duration-counter]]
            [gym.stats.counters.weekly-counter :as weekly-counter]
            [gym.stats.counters.monthly-counter :as monthly-counter]
            [gym.database :refer [db-conn redis-conn]]
            [gym.util :refer [ms->s]]
            [gym.date-utils :refer [get-week-of-year get-month get-year]]
            [taoensso.carmine :as car :refer (wcar)]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(defn get-workout-durations []
  (sql/query db-conn
             ["SELECT SUM(duration), date, user_id FROM workouts GROUP BY date, user_id"]
             {:builder-fn rs/as-unqualified-maps}))

(defn clear-counters []
  (let [keys (wcar redis-conn (car/keys "count:workout-duration*"))]
    (wcar redis-conn (mapv car/del keys))))

(defn update-counters [result]
  (let [user-id (str (:user_id result))
        duration-sec (ms->s (:sum result))
        date (.toLocalDate (:date result))
        week (get-week-of-year date)
        month (get-month date)
        year (get-year date)]

    (weekly-counter/incr-count weekly-workout-duration-counter week year user-id duration-sec)
    (monthly-counter/incr-count monthly-workout-duration-counter month year user-id duration-sec)))

(defn reinitialize-counters []
  (clear-counters)
  (dorun (map update-counters (get-workout-durations))))

(reinitialize-counters)

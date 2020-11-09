(ns gym.stats.counters.core
  (:import java.time.LocalDate
           java.time.temporal.TemporalAdjusters
           java.time.DayOfWeek)
  (:require [gym.stats.counters.in-memory-workout-duration-counter :refer [create-in-memory-workout-duration-counter]]
            [gym.workouts.repository.core :refer [workout-repository]]
            [gym.workouts.repository.workout-repository :refer [get-all-workout-durations]]
            [gym.util :refer [ms->s]]))

(defn get-current-week-durations []
  (let [today (LocalDate/now)
        start-of-week (.with today DayOfWeek/MONDAY)
        end-of-week (.with today DayOfWeek/SUNDAY)]
    (get-all-workout-durations workout-repository start-of-week end-of-week)))

(defn get-current-month-durations []
  (let [today (LocalDate/now)
        start-of-month (.with today (TemporalAdjusters/firstDayOfMonth))
        end-of-month (.with today (TemporalAdjusters/lastDayOfMonth))]
    (get-all-workout-durations workout-repository start-of-month end-of-month)))

(defn workout-durations->counter-data [results]
  (reduce
   (fn [acc result]
     (assoc acc (:user_id result) (ms->s (:sum result))))
   {}
   results))

(defonce weekly-data (atom (workout-durations->counter-data (get-current-week-durations))))
(defonce monthly-data (atom (workout-durations->counter-data (get-current-month-durations))))

(def weekly-workout-duration-counter (create-in-memory-workout-duration-counter weekly-data))
(def monthly-workout-duration-counter (create-in-memory-workout-duration-counter monthly-data))

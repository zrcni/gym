(ns gym.workouts.counters.core
  (:require [gym.workouts.counters.redis-weekly-counter :refer [create-redis-weekly-counter]]
            [gym.workouts.counters.redis-monthly-counter :refer [create-redis-monthly-counter]]
            [gym.database :refer [redis-conn]]
            [gym.workouts.counters.redis-counter :refer [create-redis-counter]]))

(def weekly-workout-duration-counter
  (create-redis-weekly-counter "workout-duration-week" (create-redis-counter redis-conn)))

(def monthly-workout-duration-counter
  (create-redis-monthly-counter "workout-duration-month" (create-redis-counter redis-conn)))

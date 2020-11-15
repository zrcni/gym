(ns gym.backend.system.workout-duration-counter-monthly
  (:require [integrant.core :as ig]
            [gym.backend.workouts.counters.redis-monthly-counter :refer [create-redis-monthly-counter]]
            [gym.backend.workouts.counters.redis-counter :refer [create-redis-counter]]))

(defmethod ig/init-key :system/workout-duration-counter-monthly [_ {:keys [redis]}]
  (create-redis-monthly-counter "workout-duration-month" (create-redis-counter redis)))

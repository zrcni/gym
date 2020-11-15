(ns gym.backend.system.workout-duration-counter-weekly
  (:require [integrant.core :as ig]
            [gym.backend.workouts.counters.redis-weekly-counter :refer [create-redis-weekly-counter]]
            [gym.backend.workouts.counters.redis-counter :refer [create-redis-counter]]))

(defmethod ig/init-key :system/workout-duration-counter-weekly [_ {:keys [redis]}]
  (create-redis-weekly-counter "workout-duration-week" (create-redis-counter redis)))

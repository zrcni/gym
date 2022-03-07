(ns gym.backend.system.workout-repo
  (:require [integrant.core :as ig]
            [gym.backend.workouts.repository.postgresql-workout-repository :refer [create-postgresql-workout-repository]]))

(defmethod ig/init-key :system/workout-repo [_ {:keys [postgres]}]
  (create-postgresql-workout-repository postgres))

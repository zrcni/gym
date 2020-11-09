(ns gym.workouts.repository.core
  (:require [gym.database :refer [db-conn]]
            [gym.events.core :refer [domain-events]]
            [gym.workouts.repository.postgresql-workout-repository :refer [create-postgresql-workout-repository]]))

(def workout-repository (create-postgresql-workout-repository db-conn domain-events))

(ns gym.workouts.controllers.core
  (:require [gym.workouts.controllers.get-workouts-by-user-id :as get-workouts-by-user-id-controller]
            [gym.workouts.controllers.get-workout-by-workout-id :as get-workout-by-workout-id-controller]
            [gym.workouts.controllers.create-workout :as create-workout-controller]
            [gym.workouts.controllers.delete-workout-by-workout-id :as delete-workout-by-workout-id-controller]
            [gym.workouts.repository.core :refer [workout-repository]]))

(def get-workouts-by-user-id (get-workouts-by-user-id-controller/create workout-repository))
(def get-workout-by-workout-id (get-workout-by-workout-id-controller/create workout-repository))
(def create-workout (create-workout-controller/create workout-repository))
(def delete-workout-by-workout-id (delete-workout-by-workout-id-controller/create workout-repository))

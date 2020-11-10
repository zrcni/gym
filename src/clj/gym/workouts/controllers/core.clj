(ns gym.workouts.controllers.core
  (:require [gym.workouts.controllers.get-workouts-by-user-id :as get-workouts-by-user-id-controller]
            [gym.workouts.controllers.get-workout-by-workout-id :as get-workout-by-workout-id-controller]
            [gym.workouts.controllers.create-workout :as create-workout-controller]
            [gym.workouts.controllers.delete-workout-by-workout-id :as delete-workout-by-workout-id-controller]
            [gym.workouts.repository.core :refer [workout-repository]]
            [gym.workouts.controllers.get-current-week-exercises-total-duration :as get-current-week-exercises-total-duration-controller]
            [gym.workouts.controllers.get-current-month-exercises-total-duration :as get-current-month-exercises-total-duration-controller]
            [gym.workouts.counters.core :refer [weekly-workout-duration-counter
                                                monthly-workout-duration-counter]]))

(def get-workouts-by-user-id (get-workouts-by-user-id-controller/create workout-repository))
(def get-workout-by-workout-id (get-workout-by-workout-id-controller/create workout-repository))
(def create-workout (create-workout-controller/create workout-repository))
(def delete-workout-by-workout-id (delete-workout-by-workout-id-controller/create workout-repository))
(def get-current-week-exercises-total-duration
  (get-current-week-exercises-total-duration-controller/create weekly-workout-duration-counter))
(def get-current-month-exercises-total-duration
  (get-current-month-exercises-total-duration-controller/create monthly-workout-duration-counter))

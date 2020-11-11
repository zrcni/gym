(ns gym.workouts.controllers.core
  (:require [gym.workouts.controllers.get-workouts-by-user-id :as get-workouts-by-user-id-controller]
            [gym.workouts.controllers.get-workout-by-workout-id :as get-workout-by-workout-id-controller]
            [gym.workouts.controllers.create-workout :as create-workout-controller]
            [gym.workouts.controllers.delete-workout-by-workout-id :as delete-workout-by-workout-id-controller]
            [gym.workouts.controllers.get-current-week-exercises-total-duration :as get-current-week-exercises-total-duration-controller]
            [gym.workouts.controllers.get-current-month-exercises-total-duration :as get-current-month-exercises-total-duration-controller]))

(defn get-workouts-by-user-id [workout-repository]
  (get-workouts-by-user-id-controller/create workout-repository))

(defn get-workout-by-workout-id [workout-repository]
  (get-workout-by-workout-id-controller/create workout-repository))

(defn create-workout [workout-repository]
  (create-workout-controller/create workout-repository))

(defn delete-workout-by-workout-id [workout-repository]
  (delete-workout-by-workout-id-controller/create workout-repository))

(defn get-current-week-exercises-total-duration [workout-duration-counter-weekly]
  (get-current-week-exercises-total-duration-controller/create workout-duration-counter-weekly))

(defn get-current-month-exercises-total-duration [workout-duration-counter-monthly]
  (get-current-month-exercises-total-duration-controller/create workout-duration-counter-monthly))

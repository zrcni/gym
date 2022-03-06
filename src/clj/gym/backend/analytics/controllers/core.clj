(ns gym.backend.analytics.controllers.core
  (:require [gym.backend.analytics.controllers.workout-duration-by-tag :as workout-duration-by-tag-controller]
            [gym.backend.analytics.controllers.workouts-by-day-of-week :as workouts-by-day-of-week-controller]
            [gym.backend.analytics.controllers.workouts-by-month-of-year :as workouts-by-month-of-year-controller]))

(defn workout-duration-by-tag [postgres]
  (workout-duration-by-tag-controller/create postgres))

(defn workouts-by-day-of-week [postgres]
  (workouts-by-day-of-week-controller/create postgres))

(defn workouts-by-month-of-year [postgres]
  (workouts-by-month-of-year-controller/create postgres))

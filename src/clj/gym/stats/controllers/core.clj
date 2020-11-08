(ns gym.stats.controllers.core
  (:require [gym.stats.controllers.get-current-week-exercises-total-duration :as get-current-week-exercises-total-duration-controller]
            [gym.stats.controllers.get-current-month-exercises-total-duration :as get-current-month-exercises-total-duration-controller]))

(def get-current-week-exercises-total-duration (get-current-week-exercises-total-duration-controller/create))
(def get-current-month-exercises-total-duration (get-current-month-exercises-total-duration-controller/create))

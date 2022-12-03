(ns gym.frontend.calendar.db
  (:require
   [cljs-time.core :as t]
   [gym.frontend.local-storage :refer [ls-get]]
   [gym.frontend.calendar-utils :refer [calculate-weeks calculate-start-date num-weeks]]))

(defn make-default-db []
  (let [start-date (calculate-start-date (t/now) num-weeks)]
    {:loading true
     :start-date start-date
     :editing-index nil
     :weeks (calculate-weeks start-date)
     :workouts nil
     :suggested-workout-tags (ls-get :suggested-workout-tags)}))

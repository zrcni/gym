(ns gym.frontend.home.db
  (:require
   [cljs-time.core :as t]
   [gym.frontend.calendar-utils :refer [calculate-weeks calculate-start-date num-weeks]]))

(def default-db
    (let [start-date (calculate-start-date (t/now) num-weeks)]
      {:calendar {:loading true
                  :start-date start-date
                  :editing-index nil
                  :weeks (calculate-weeks start-date)
                  :workouts nil}}))

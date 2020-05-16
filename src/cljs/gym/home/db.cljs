(ns gym.home.db
  (:require
   [cljs-time.core :as t]
   [gym.calendar-utils :refer [calculate-weeks calculate-start-date num-weeks]]))

(def default-db
    (let [start-date (calculate-start-date (t/now) num-weeks)]
      {:stats nil
       :calendar {:start-date start-date
                  :editing-index nil
                  :weeks (calculate-weeks start-date)
                  :workouts nil}}))

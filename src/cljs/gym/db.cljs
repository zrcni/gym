(ns gym.db
  (:require
   [cljs-time.core :as t]
   [gym.calendar-utils :refer [calculate-weeks start-of-week]]))

(def default-db
  (let [start-date (start-of-week (t/now))]
    {:current-route nil
     :user nil
     :token nil
     :login-status "WAITING"
     :stats nil
     :calendar {:start-date start-date
                :editing-index nil
                :weeks (calculate-weeks start-date)
                :workouts nil}}))

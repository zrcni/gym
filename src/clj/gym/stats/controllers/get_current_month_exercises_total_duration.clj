(ns gym.stats.controllers.get-current-month-exercises-total-duration
  (:require [gym.stats.counter :refer [current-month-exercise-durations]]))

(defn create []
  (fn [req]
   (let [user-id (get-in req [:context :user :user_id])
         duration ((-> current-month-exercise-durations :get) user-id)]

     {:status 200
      :body {:duration duration}
      :headers {"Content-Type" "application/json"}})))

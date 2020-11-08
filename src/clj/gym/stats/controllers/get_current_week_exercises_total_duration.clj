(ns gym.stats.controllers.get-current-week-exercises-total-duration
  (:require [gym.stats.counter :refer [current-week-exercise-durations]]))

(defn create []
  (fn [req]
   (let [user-id (get-in req [:context :user :user_id])
         duration ((-> current-week-exercise-durations :get) user-id)]
  
     {:status 200
      :body {:duration duration}
      :headers {"Content-Type" "application/json"}})))

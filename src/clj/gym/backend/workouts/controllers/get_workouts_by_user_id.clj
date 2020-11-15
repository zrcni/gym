(ns gym.backend.workouts.controllers.get-workouts-by-user-id
  (:require
   [gym.backend.workouts.repository.workout-repository :refer [get-workouts-by-user-id]]))

(defn create [workout-repository]
 (fn [req]
   (let [user-id (get-in req [:context :user :user_id])
         workouts  (get-workouts-by-user-id workout-repository user-id)]

     {:status 200
      :headers {"Content-Type" "application/json"}
      :body workouts})))

(ns gym.backend.workouts.get-workouts-by-user-id
  (:require
   [gym.backend.workouts.repository.workout-repository :refer [get-workouts-by-user-id]]))

(defn controller [req]
  (let [repo (-> req :deps :workout-repo)
        user-id (get-in req [:user :user_id])
        workouts  (get-workouts-by-user-id repo user-id)]

    {:status 200
     :body workouts}))

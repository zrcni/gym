(ns gym.backend.workouts.get-workout-by-workout-id
  (:require
   [gym.backend.workouts.repository.workout-repository :refer [get-workout-by-workout-id]]))

(defn controller [req]
  (let [repo (-> req :deps :workout-repo)
        workout-id (get-in req [:path-params :workout-id])
        workout (get-workout-by-workout-id repo workout-id)]

    {:status 200
     :body workout}))

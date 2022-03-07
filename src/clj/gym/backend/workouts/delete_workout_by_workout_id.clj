(ns gym.backend.workouts.delete-workout-by-workout-id
  (:require [gym.backend.workouts.repository.workout-repository :refer [delete-workout-by-workout-id!]]))

(defn controller [req]
  (let [repo (-> req :deps :workout-repo)
        workout-id (get-in req [:path-params :workout-id])]

    (if (> (delete-workout-by-workout-id! repo workout-id) 0)
      {:status 204}
      {:status 404})))

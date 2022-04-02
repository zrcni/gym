(ns gym.backend.workouts.delete-workout-by-workout-id
  (:require [gym.backend.logger :as log]
            [gym.backend.workouts.repository.workout-repository :refer [delete-workout-by-workout-id!]]))

(defn controller [req]
  (let [repo (-> req :deps :workout-repo)
        user-id (get-in req [:user :user_id])
        workout-id (get-in req [:path-params :workout-id])
        deleted? (> (delete-workout-by-workout-id! repo workout-id) 0)]

    (if deleted?
      (do 
        (log/info "exercise deleted" {:user-id user-id})
        {:status 204})
      {:status 404})))

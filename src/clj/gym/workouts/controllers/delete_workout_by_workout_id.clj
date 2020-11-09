(ns gym.workouts.controllers.delete-workout-by-workout-id
  (:require [gym.workouts.repository.workout-repository :refer [delete-workout-by-workout-id!]]))

(defn create [workout-repository]
 (fn [req]
   (let [workout-id (get-in req [:path-params :workout-id])]

     (if (> (delete-workout-by-workout-id! workout-repository workout-id) 0)
       {:status 204}
       {:status 404}))))

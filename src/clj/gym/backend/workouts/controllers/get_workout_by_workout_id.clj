(ns gym.backend.workouts.controllers.get-workout-by-workout-id
  (:require
   [gym.backend.workouts.repository.workout-repository :refer [get-workout-by-workout-id]]))

(defn create [workout-repository]
 (fn [req]
   (let [workout-id (get-in req [:path-params :workout-id])
         workout (get-workout-by-workout-id workout-repository workout-id)]

     {:status 200
      :headers {"Content-Type" "application/json"}
      :body workout})))

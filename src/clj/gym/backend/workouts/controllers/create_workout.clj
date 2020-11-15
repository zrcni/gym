(ns gym.backend.workouts.controllers.create-workout
  (:require
   [clojure.walk :refer [keywordize-keys]]
   [gym.workout :refer [make-workout]]
   [gym.backend.workouts.repository.workout-repository :refer [create-workout!]]))


(defn create [workout-repository]
 (fn [req]
   (let [body (keywordize-keys (:body req))
         create-args (assoc body :user_id (get-in req [:context :user :user_id]))
         workout (create-workout! workout-repository (make-workout create-args))]

     {:status 201
      :headers {"Content-Type" "application/json"}
      :body workout})))

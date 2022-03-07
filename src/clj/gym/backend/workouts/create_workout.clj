(ns gym.backend.workouts.create-workout
  (:require
   [clojure.walk :refer [keywordize-keys]]
   [gym.workout :refer [make-workout]]
   [gym.backend.workouts.repository.workout-repository :refer [create-workout!]]))


(defn controller [req]
  (let [repo (-> req :deps :workout-repo)
        body (keywordize-keys (:body req))
        create-args (assoc body :user_id (get-in req [:user :user_id]))
        workout (create-workout! repo (make-workout create-args))]

    {:status 201
     :body workout}))

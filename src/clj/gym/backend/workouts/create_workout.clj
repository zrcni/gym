(ns gym.backend.workouts.create-workout
  (:require [gym.backend.logger :as log]
            [clojure.walk :refer [keywordize-keys]]
            [gym.workout :refer [make-workout]]
            [gym.backend.workouts.repository.workout-repository :refer [create-workout!]]))


(defn controller [req]
  (let [repo (-> req :deps :workout-repo)
        body (keywordize-keys (:body req))
        user-id (get-in req [:user :user_id])
        create-args (assoc body :user_id user-id)
        workout (create-workout! repo (make-workout create-args))]

    (log/info "exercise created" {:user-id user-id})

    {:status 201
     :body workout}))

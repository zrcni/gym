(ns gym.api
  (:require
   [clojure.walk :refer [keywordize-keys]]
   [gym.workouts :as workouts]))

(defn get-user-id [request]
  (-> request :token-payload :sub))

(defn get-user-workouts-handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (workouts/get-by-user-id (get-user-id request))})

(defn get-workout-by-id-handler [request]
  (let [workout-id (get-in request [:path-params :workout-id])
        workout (workouts/get-by-id workout-id)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body workout}))

(defn create-workout-handler [request]
  (let [body (keywordize-keys (:body request))
        create-args (assoc body :user_id (get-user-id request))
        workout (workouts/create! create-args)]
    {:status 201
     :headers {"Content-Type" "application/json"}
     :body workout}))

(defn delete-workout-by-id-handler [request]
  (let [workout-id (-> request :path-params :workout-id)]
    (if (> (workouts/delete-by-id! workout-id) 0)
      {:status 204}
      {:status 404})))

(ns gym.api
  (:require
   [clojure.walk :refer [keywordize-keys]]
   [gym.workouts :as workouts]))

(defn get-workouts-handler [_request]
  {:status 200
    :headers {"Content-Type" "application/json"}
    :body (workouts/get-all)})

(defn get-workout-by-id-handler [request]
  (let [workout-id (get-in request [:path-params :workout-id])
        workout (workouts/get-by-id workout-id)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body workout}))

(defn create-workout-handler [request]
  (let [body (keywordize-keys (:body request))
        workout (workouts/create! body)]
    {:status 201
     :headers {"Content-Type" "application/json"}
     :body workout}))

(defn delete-workout-by-id-handler [request]
  (let [workout-id (get-in request [:path-params :workout-id])
        deleted-count (workouts/delete-by-id! workout-id)]
    (if (> deleted-count 0)
      {:status 204}
      {:status 404})))

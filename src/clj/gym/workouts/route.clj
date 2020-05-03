(ns gym.workouts.route
  (:require
   [gym.workouts.repository :refer [get-by-user-id get-by-id create! delete-by-id!]]
   [clojure.walk :refer [keywordize-keys]]
   [gym.auth :refer [get-token-user-id]]))

(defn ^:private get-user-workouts-handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (get-by-user-id (get-token-user-id request))})

(defn ^:private get-workout-by-id-handler [request]
  (let [workout-id (get-in request [:path-params :workout-id])
        workout (get-by-id workout-id)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body workout}))

(defn ^:private create-workout-handler [request]
  (let [body (keywordize-keys (:body request))
        create-args (assoc body :user_id (get-token-user-id request))
        workout (create! create-args)]
    {:status 201
     :headers {"Content-Type" "application/json"}
     :body workout}))

(defn ^:private delete-workout-by-id-handler [request]
  (let [workout-id (-> request :path-params :workout-id)]
    (if (> (delete-by-id! workout-id) 0)
      {:status 204}
      {:status 404})))

(defn create-workouts-route [path]
  [path
   ["" {:get {:handler get-user-workouts-handler}
        :post {:handler create-workout-handler}}]
   ["/:workout-id" {:get {:handler get-workout-by-id-handler}
                    :delete {:handler delete-workout-by-id-handler}}]])

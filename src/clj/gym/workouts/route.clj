(ns gym.workouts.route
  (:require
   [gym.middleware :refer [wrap-user]]
   [gym.workouts.repository :refer [get-by-user-id get-by-id create! delete-by-id!]]
   [clojure.walk :refer [keywordize-keys]]))

(defn ^:private get-user-workouts-handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (get-by-user-id (-> request :context :user :user_id))})

(defn ^:private get-workout-by-id-handler [request]
  (let [workout-id (get-in request [:path-params :workout-id])
        workout (get-by-id workout-id)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body workout}))

(defn ^:private create-workout-handler [request]
  (let [body (keywordize-keys (:body request))
        create-args (assoc body :user_id (-> request :context :user :user_id))
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
   ["" {:get {:handler get-user-workouts-handler
              :middleware [wrap-user]}
        :post {:handler create-workout-handler
               :middleware [wrap-user]}}]
   ["/:workout-id" {:get {:handler get-workout-by-id-handler}
                    :delete {:handler delete-workout-by-id-handler
                             :middleware [wrap-user]}}]])

(ns gym.users.route
  [:require
   [gym.users.repository :refer [get-by-token-user-id]]
   [gym.auth :refer [get-token-user-id]]])

(defn get-user-via-token [request]
  (let [user (get-by-token-user-id (get-token-user-id request))]
    (if user
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body user}
      {:status 404})))

(defn create-users-route [path]
  [path
   ["/token" {:get {:handler get-user-via-token}}]])

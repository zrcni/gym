(ns gym.users.controllers.get-authenticated-user
  (:require    [gym.users.repository :refer [get-by-token-user-id]]
               [gym.auth :refer [get-token-user-id]]))


(defn create []
  (fn [request]
    (let [user-id (get-token-user-id request)
          user (get-by-token-user-id user-id)]

      (if user
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body user}
        {:status 404}))))
(ns gym.users.controllers.get-authenticated-user
  (:require    [gym.users.repository.user-repository :refer [get-user-by-token-user-id]]
               [gym.auth.utils :refer [get-token-user-id]]))


(defn create [user-repository]
  (fn [request]
    (let [user-id (get-token-user-id request)
          user (get-user-by-token-user-id user-repository user-id)]

      (if user
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body user}
        {:status 404}))))

(ns gym.users.controllers.get-authenticated-user
  (:require [gym.users.repository.user-repository :refer [get-user-by-token-user-id]]))


(defn create [user-repository]
  (fn [request]
    (let [token-user-id (-> request :token-payload :sub)
          user (get-user-by-token-user-id user-repository token-user-id)]

      (if user
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body user}
        {:status 404}))))

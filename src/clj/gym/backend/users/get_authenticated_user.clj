(ns gym.backend.users.get-authenticated-user
  (:require [gym.backend.users.repository.user-repository :refer [get-user-by-token-user-id]]))

(defn controller [req]
  (let [repo (-> req :deps :user-repo)
        token-user-id (-> req :token-payload :sub)
        user (get-user-by-token-user-id repo token-user-id)]

    (if user
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body user}
      {:status 404})))

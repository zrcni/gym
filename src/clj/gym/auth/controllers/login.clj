(ns gym.auth.controllers.login
  (:require [clj-http.client :as http]
            [gym.auth.utils :refer [get-token-user-id get-token-payload headers->token]]
            [gym.users.repository.user-repository :refer [get-user-by-token-user-id create-user!]]
            [gym.users.repository.core :refer [user-repository]]))

(defn parse-auth0-user-info [user-info]
  {:username (:nickname user-info)
   :token-user-id (:sub user-info)
   :avatar-url (:picture user-info)})

;; TODO: handle non-200 response
(defn get-auth0-user-info [{:keys [issuer token]}]
  (let [response (http/get (str issuer "userinfo")
                           {:as :json
                            :headers {"Authorization" (str "Bearer " token)}})]
    (:body response)))

(defn create-new-user [request]
  (let [payload (get-token-payload request)
        user-info (get-auth0-user-info {:issuer (:iss payload)
                                        :token (headers->token (:headers request))})
        new-user (create-user! user-repository (parse-auth0-user-info user-info))]
    new-user))

(defn create []
  (fn [req]
    (let [user (or
                (get-user-by-token-user-id user-repository (get-token-user-id req))
                (create-new-user req))]

      {:status 200
       :headers {"Content-Type" "application/json"}
       :body {:user user}})))
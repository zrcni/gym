(ns gym.backend.auth.login
  (:require [gym.backend.logger :as log]
            [clj-http.client :as http]
            [gym.backend.auth.utils :refer [sub->token-source get-token-payload headers->token]]
            [gym.backend.users.repository.user-repository :refer [get-user-by-token-user-id create-user!]]))

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

(defn create-new-user [user-repository request]
  (let [payload (get-token-payload request)
        user-info (get-auth0-user-info {:issuer (:iss payload)
                                        :token (headers->token (:headers request))})
        new-user (create-user! user-repository (parse-auth0-user-info user-info))]
    new-user))

(defn controller [req]
  (let [repo (-> req :deps :user-repo)
        user (or
              (get-user-by-token-user-id repo (-> req :token-payload :sub))
              (create-new-user repo req))]

    (log/info "user logged in" {:user-id (:user_id user)
                                :token-iss (-> req :token-payload :iss)
                                :token-iat (-> req :token-payload :iat)
                                :token-exp (-> req :token-payload :exp)
                                :token-source (-> req :token-payload :sub sub->token-source)})

    {:status 200
     :body {:user user}}))

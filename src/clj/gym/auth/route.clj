(ns gym.auth.route
  [:require
   [clj-http.client :as http]
   [gym.auth :refer [get-token-user-id get-token-payload headers->token]]
   [gym.users.repository :refer [get-by-token-user-id create!]]])

(defn parse-auth0-user-info [user-info]
  {:username (:nickname user-info)
   :token_user_id (:sub user-info)
   :avatar_url (:picture user-info)})

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
        new-user (create! (parse-auth0-user-info user-info))]
    new-user))

(defn login [request]
  (let [user (or
              (get-by-token-user-id (get-token-user-id request))
              (create-new-user request))]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body {:user user}}))

(defn create-auth-route [path]
  [path
   ["/login" {:post {:handler login}}]])
(ns gym.backend.middleware
  (:require
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [buddy.sign.jwt :as jwt]
   [gym.backend.config :as cfg]
   [gym.backend.users.repository.user-repository :refer [get-user-by-token-user-id]]
   [gym.backend.auth.utils :refer [get-public-key headers->token]]   
   [gym.backend.date-utils :refer [instant]]))

(defn pretty-request [request]
  (-> (str "method: " (:request-method request))
      (str " - uri: " (:uri request))
      (str " - origin: " (get-in request [:headers "origin"]))
      (str " - user-agent: " (get-in request [:headers "user-agent"]))
      (str " - content-length: " (or (get-in request [:headers "content-length"] 0)))
      (str " - timestamp: " (str (instant)))))

(defn wrap-log [handler]
  (fn [request]
    (println (pretty-request request))
    (handler request)))

(def web-middlewares
  [#(wrap-defaults % site-defaults)])

(defn parse-token [token]
  (jwt/unsign token (get-public-key) {:alg :rs256}))

;; array of regex patterns
(def allowed-origins (mapv #(re-pattern %) (conj cfg/frontend-urls cfg/host-url)))

(defn unauthorized-response [& [message]]
  {:status 401
   :body {:error (or message "Unauthorized")}})

(defn handle-token [handler request token]
  (if token
    (let [token-payload (parse-token token)]
      (handler (assoc request :token-payload token-payload)))
    (unauthorized-response)))

;; (defn handle-token [handler request token]
;;   (try
;;     (if token
;;       (let [token-payload (parse-token token)]
;;         (handler (assoc request :token-payload token-payload)))
;;       (unauthorized-response))
;;     (catch Exception e
;;       (println (str "handle-auth-header exception: " e))
;;       (unauthorized-response "Invalid token"))))

(defn wrap-token [handler]
  (fn [request]
    (if-let [token (headers->token (:headers request))]
      (handle-token handler request token)
      (unauthorized-response))))

;; expected to be used after wrap-token middleware
(defn wrap-user [user-repository]
  (fn [handler]
    (fn [req]
      (let [token-user-id (-> req :token-payload :sub)
            user (get-user-by-token-user-id user-repository token-user-id)]
        (handler (assoc-in req [:context :user] user))))))

(def api-middlewares
  [wrap-log
   #(wrap-cors % :access-control-allow-origin allowed-origins
               :access-control-allow-methods [:get :post :put :delete :options])
   wrap-json-response
   wrap-json-body
   wrap-token])

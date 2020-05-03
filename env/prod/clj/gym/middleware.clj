(ns gym.middleware
  (:import java.time.Instant)
  (:require
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [gym.config :as cfg]
   [buddy.sign.jwt :as jwt]
   [gym.users.repository :refer [get-by-token-user-id]]
   [gym.auth :refer [get-public-key headers->token get-token-user-id]]   
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn pretty-request [request]
  (-> (str "method: " (:request-method request))
      (str " - uri: " (:uri request))
      (str " - origin: " (get-in request [:headers "origin"]))
      (str " - user-agent: " (get-in request [:headers "user-agent"]))
      (str " - content-length: " (or (get-in request [:headers "content-length"] 0)))
      (str " - timestamp: " (.toString (Instant/now)))))

(defn wrap-log [handler]
  (fn [request]
    (println (pretty-request request))
    (handler request)))

(def web-middlewares
  [#(wrap-defaults % site-defaults)])

(defn parse-token [token]
  (jwt/unsign token (get-public-key) {:alg :rs256}))

;; array of regex patterns
(def allowed-origins ((comp vec flatten vector)
                           [(map #(re-pattern %) cfg/frontend-urls) (re-pattern cfg/host-url)]))

(def api-middlewares
  [wrap-log
   #(wrap-cors % :access-control-allow-origin allowed-origins
               :access-control-allow-methods [:get :post :put :delete :options])
   wrap-json-response
   wrap-json-body])

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
(defn wrap-user [handler]
  (fn [request]
    (let [token-user-id (get-token-user-id request)
          user (get-by-token-user-id token-user-id)]
      (handler (assoc-in request [:context :user] user)))))

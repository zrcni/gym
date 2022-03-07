(ns gym.backend.middleware
  (:require [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [prone.middleware :refer [wrap-exceptions]]
            [buddy.sign.jwt :as jwt]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [gym.backend.users.repository.user-repository :refer [get-user-by-token-user-id]]
            [gym.backend.auth.utils :refer [get-public-key headers->token]]
            [gym.backend.date-utils :refer [instant]]))

(defn parse-token [token]
  (jwt/unsign token (get-public-key) {:alg :rs256}))

(def web-middlewares
  [#(wrap-defaults % site-defaults)
   wrap-exceptions
   wrap-reload])

(defn pretty-request [request]
  (-> (str "method: " (:request-method request))
      (str " - uri: " (:uri request))
      (str " - origin: " (get-in request [:headers "origin"]))
      (str " - user-agent: " (get-in request [:headers "user-agent"]))
      (str " - content-length: " (get-in request [:headers "content-length"] 0))
      (str " - timestamp: " (str (instant)))))

(defn wrap-log [handler]
  (fn [request]
    (println (pretty-request request))
    (handler request)))

(defn unauthorized-response [& [message]]
  {:status 401
   :body {:error (or message "Unauthorized")}})

(defn handle-token [handler request token]
  (if token
    (let [token-payload (parse-token token)]
      (handler (assoc request :token-payload token-payload)))
    (unauthorized-response)))

(defn wrap-token [handler]
  (fn [request]
    (if-let [token (headers->token (:headers request))]
      (handle-token handler request token)
      (unauthorized-response))))

;; expected to be used after wrap-token middleware
(defn wrap-user [handler]
  (fn [req]
    (let [repo (-> req :deps :user-repo)
          token-user-id (-> req :token-payload :sub)
          user (get-user-by-token-user-id repo token-user-id)]
      (handler (assoc req :user user)))))

(defn wrap-prop
  "Add a property to the request object."
  [kw val]
  (fn [handler]
    (fn [req]
      (handler (assoc req kw val)))))

(def api-middlewares
  [wrap-log
   #(wrap-cors % :access-control-allow-origin #"http://localhost:3001"
               :access-control-allow-methods [:get :put :post :delete :options])
   wrap-json-response
   wrap-json-body
   wrap-token])

;; (defn handle-token [handler request token]
;;   (try
;;     (if token
;;       (let [token-payload (parse-token token)]
;;         (handler (assoc request :token-payload token-payload)))
;;       (unauthorized-response))
;;     (catch Exception e
;;       (println (str "handle-auth-header exception: " e))
;;       (unauthorized-response "Invalid token"))))

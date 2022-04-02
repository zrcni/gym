(ns gym.backend.middleware
  (:require
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.nested-params :refer [wrap-nested-params]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [buddy.sign.jwt :as jwt]
   [gym.backend.config :as cfg]
   [gym.backend.users.repository.user-repository :refer [get-user-by-token-user-id]]
   [gym.backend.auth.utils :refer [get-public-key headers->token]]
   [gym.backend.logger :as log]
   [gym.util :refer [generate-uuid get-url-origin]]))

(defn wrap-req-id
  "Generates a UUID and adds it to :req-id property of the request object."
  [handler]
  (fn [req]
    (handler (assoc req :req-id (generate-uuid)))))

(defn wrap-logger-context
  [handler]
  (fn [req]
    (log/with-context {:request-id (:req-id req)}
      (handler req))))

(defn format-request-log [request]
  {:method (:request-method request)
   :uri (:uri request)
   :origin (get-in request [:headers "origin"]
                   (get-url-origin (get-in request [:headers "referer"])))
   :referer (get-in request [:headers "referer"])
   :user-agent (get-in request [:headers "user-agent"])
   :content-length (get-in request [:headers "content-length"] 0)})

(defn wrap-log [handler]
  (fn [request]
    (log/info "request received" (format-request-log request))
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
  [#(wrap-cors % :access-control-allow-origin allowed-origins
               :access-control-allow-methods [:get :post :put :delete :options])
   wrap-params
   wrap-nested-params
   wrap-keyword-params
   wrap-content-type
   wrap-json-response
   wrap-json-body
   wrap-token
   wrap-req-id
   wrap-logger-context
   wrap-log])

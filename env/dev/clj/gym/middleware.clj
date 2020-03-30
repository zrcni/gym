(ns gym.middleware
  (:require
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [prone.middleware :refer [wrap-exceptions]]
   [clojure.string :as string]
   [buddy.sign.jwt :as jwt]
   [buddy.core.keys :as keys]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

;; source: https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com
(def token-kid (keys/public-key "./certs/public-key.pem"))

(defn parse-token [token]
  (jwt/unsign token token-kid {:alg :rs256}))

(def web-middlewares
  [#(wrap-defaults % site-defaults)
   wrap-exceptions
   wrap-reload])

(def api-middlewares
  [#(wrap-cors % :access-control-allow-origin #"http://localhost:3449"
               :access-control-allow-methods [:get :post :delete])
   wrap-json-response
   wrap-json-body])

(defn forbidden-response []
  {:status 403
   :body {:error "Forbidden"}})

(defn handle-auth-header [handler request auth-header]
  (let [[_prefix token] (string/split auth-header #" ")]
    (if-let [token-payload (parse-token token)]
      (handler (assoc request :token-payload token-payload))
      (forbidden-response))))

(defn wrap-token [handler]
  (fn [request]
    (if-let [auth-header (get-in request [:headers "authorization"])]
      (handle-auth-header handler request auth-header)
      (forbidden-response))))

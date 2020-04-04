(ns gym.middleware
  (:require
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [clojure.string :as string]
   [gym.config :as cfg]
   [buddy.sign.jwt :as jwt]
   [buddy.core.keys :as keys]
   [gym.jwt :refer [get-token]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(def web-middlewares
  [#(wrap-defaults % site-defaults)])

(defn parse-token [token]
  (jwt/unsign token (keys/str->public-key (get-token)) {:alg :rs256}))

(def api-middlewares
  [#(wrap-cors % :access-control-allow-origin (re-pattern (str "$" cfg/frontend-url))
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

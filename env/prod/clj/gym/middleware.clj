(ns gym.middleware
  (:import java.time.Instant)
  (:require
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [clojure.string :as string]
   [gym.config :as cfg]
   [buddy.sign.jwt :as jwt]
   [buddy.core.keys :as keys]
   [gym.jwt :refer [get-token]]
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
  (jwt/unsign token (keys/str->public-key (get-token)) {:alg :rs256}))

(def api-middlewares
  [wrap-log
   ;; TODO: provide server url via environment
   #(wrap-cors % :access-control-allow-origin (re-pattern (str "(^" cfg/frontend-url "|" "https://damp-thicket-94785.herokuapp.com" ")"))
               :access-control-allow-methods [:get :post :delete :options])
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

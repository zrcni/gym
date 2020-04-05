(ns gym.middleware
  (:import java.time.Instant)
  (:require
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [prone.middleware :refer [wrap-exceptions]]
   [clojure.string :as string]
   [buddy.sign.jwt :as jwt]
   [buddy.core.keys :as keys]
   [gym.jwt :refer [get-token]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))


(defn parse-token [token]
  (jwt/unsign token (keys/str->public-key (get-token)) {:alg :rs256}))

(def web-middlewares
  [#(wrap-defaults % site-defaults)
   wrap-exceptions
   wrap-reload])

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

(def api-middlewares
  [wrap-log
   #(wrap-cors % :access-control-allow-origin #"http://localhost:3449"
               :access-control-allow-methods [:get :post :delete])
   wrap-json-response
   wrap-json-body])

(defn forbidden-response []
  {:status 403
   :body {:error "Forbidden"}})

(defn handle-auth-header [handler request auth-header]
  (try
    (let [[_prefix token] (string/split auth-header #" ")
          token-payload (parse-token token)]
      (handler (assoc request :token-payload token-payload)))
    (catch clojure.lang.ExceptionInfo e
      (println (str "[ERROR] handle-auth-header: " e))
      (forbidden-response))))

(defn wrap-token [handler]
  (fn [request]
    (if-let [auth-header (get-in request [:headers "authorization"])]
      (handle-auth-header handler request auth-header)
      (forbidden-response))))

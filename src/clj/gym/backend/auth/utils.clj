(ns gym.backend.auth.utils
  (:require [clojure.string :refer [split]]
            [gym.backend.config :as cfg]
            [buddy.core.keys :as keys]
            [buddy.sign.jwt :as jwt]))

;; Need to create the cert lazily, because keys/str->public-key throws
;; an exception if the public key is invalid, wihch is during the build.
(def ^:private cert (atom nil))

(defn get-public-key []
  (when-not @cert
    (reset! cert (keys/str->public-key cfg/auth0-public-key)))
  @cert)

(defn headers->token [headers]
  (when-let [authorization (get headers "authorization")]
    (second (split authorization #" "))))

(defn parse-token [token]
  (jwt/unsign token (get-public-key) {:alg :rs256}))

(defn get-token-payload [request]
  (-> request
      :headers
      (headers->token)
      (parse-token)))

(defn sub->token-source
  "e.g. \"github|12345\" returns \"github\""
  [sub]
  (-> sub  (split #"\|") first))

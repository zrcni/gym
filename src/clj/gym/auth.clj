(ns gym.auth
  (:require
   [clojure.string :as string]
   [gym.config :as cfg]
   [buddy.core.keys :as keys]))

;; Need to create the cert lazily, because keys/str->public-key throws
;; an exception if the public key is invalid, wihch is during the build.
(def ^:private cert (atom nil))

(defn get-public-key []
  (when (nil? @cert)
    (reset! cert (keys/str->public-key cfg/public-key)))
  @cert)

(defn get-token-payload [request]
  (:token-payload request))

(defn get-token-user-id [request]
  (:sub (get-token-payload request)))

(defn headers->token [headers]
  (let [[_prefix token] (string/split (get headers "authorization") #" ")]
    token))

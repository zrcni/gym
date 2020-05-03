(ns gym.auth
  (:require
   [clojure.string :as string]
   [gym.config :as cfg]
   [buddy.core.keys :as keys]))

(def ^:private cert (keys/str->public-key cfg/public-key))

(defn get-public-key [] cert)

(defn get-token-payload [request]
  (:token-payload request))

(defn get-token-user-id [request]
  (:sub (get-token-payload request)))

(defn headers->token [headers]
  (let [[_prefix token] (string/split (get headers "authorization") #" ")]
       token))

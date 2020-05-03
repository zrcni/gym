(ns gym.auth
  (:require
   [gym.config :as cfg]
   [buddy.core.keys :as keys]))

(def ^:private cert (keys/str->public-key cfg/public-key))

(defn get-public-key [] cert)

(defn get-token-payload [request]
  (:token-payload request))

(defn get-token-user-id [request]
  (:sub (get-token-payload request)))

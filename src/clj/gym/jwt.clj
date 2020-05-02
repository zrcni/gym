(ns gym.jwt
  (:require
   [gym.config :as cfg]
   [buddy.core.keys :as keys]))

(def ^:private cert (keys/str->public-key cfg/public-key))

(defn get-public-key [] cert)

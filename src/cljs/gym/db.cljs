(ns gym.db
  (:require
   [gym.home.db :as home]))

(def default-db
  {:current-route nil
   :user nil
   :token nil
   :auth-status :waiting
   :home home/default-db})

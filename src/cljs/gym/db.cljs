(ns gym.db
  (:require
   [gym.styles :as styles]
   [gym.home.db :as home]))

(def default-db
  {:theme {:accent-color styles/accent-color
           :accent-color-hover styles/accent-color-hover
           :accent-color-active styles/accent-color-active}
   :current-route nil
   :user nil
   :token nil
   :auth-status :waiting
   :home home/default-db})

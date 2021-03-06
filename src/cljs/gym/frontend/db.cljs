(ns gym.frontend.db
  (:require
   [gym.frontend.styles :as styles]
   [gym.frontend.home.db :as home]))

(def default-db
  {:theme {:theme-color styles/theme-color
           :theme-color-hover styles/theme-color-hover
           :theme-color-active styles/theme-color-active
           ;; Changed, but not saved yet
           :preview? false}
   :current-route nil
   :user nil
   :token nil
   :auth-status :waiting
   :home home/default-db})

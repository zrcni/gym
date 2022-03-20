(ns gym.frontend.db
  (:require
   [gym.frontend.styles :as styles]
   [gym.frontend.home.calendar.db :as calendar]
   [gym.frontend.analytics.db :as analytics]))

(def default-db
  {:theme {:theme-color styles/theme-color
           :theme-color-hover styles/theme-color-hover
           :theme-color-active styles/theme-color-active
           ;; Changed, but not saved yet
           :preview? false}
   :user-prefs nil
   :current-route nil
   :user nil
   :token nil
   :auth-status :waiting
   :calendar calendar/default-db
   :analytics analytics/default-db
   :all-tags nil})

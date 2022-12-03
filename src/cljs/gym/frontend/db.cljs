(ns gym.frontend.db
  (:require
   [gym.frontend.styles :as styles]
   [gym.frontend.calendar.db :as calendar-db]
   [gym.frontend.analytics.db :as analytics-db]))

(defn make-default-db []
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
   :calendar (calendar-db/make-default-db)
   :analytics (analytics-db/make-default-db)
   :all-tags nil})

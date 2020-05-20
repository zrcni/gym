(ns gym.error-reporting
  (:require
   [gym.config :as cfg]
   [re-frame.core :refer [reg-fx reg-event-fx]]))

(reg-event-fx ::init!
              (fn [_ _]
                (.init js/Sentry (clj->js {:dsn cfg/sentry-dsn
                                           :release (str "exercise-tracker-web@" cfg/commit-sha)}))))

(reg-fx ::set-sentry-user-info!
        (fn [user-info]
          (.configureScope js/Sentry (fn [^js/SentryScope scope]
                                       (.setUser scope (clj->js user-info))))))

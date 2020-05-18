(ns gym.core
  (:require
   [gym.config :as cfg]
   [gym.error-reporting]
   [gym.effects]
   [gym.events]
   [gym.subs]
   [gym.styles :refer [inject-global-styles]]
   [gym.auth :refer [create-auth0-client reg-auth0-cofx]]
   [reagent.core :as reagent]
   [re-frame.core :refer [dispatch dispatch-sync clear-subscription-cache!]]
   [gym.router :as router]))

(defn mount-root []
  (inject-global-styles)
  (reagent/render [router/root] (.getElementById js/document "app")))

(defn init! []
  (dispatch-sync [:initialize-db])
  (reg-auth0-cofx (create-auth0-client))
  (prn "sentry: " cfg/sentry-dsn)
  (when cfg/sentry-dsn (dispatch [:init-error-reporting!]))
  (clear-subscription-cache!)
  (router/start!)
  (mount-root))

(ns gym.core
  (:require
   [gym.config :as cfg]
   [gym.error-reporting]
   [gym.effects]
   [gym.events]
   [gym.subs]
   [gym.theme]
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
  (dispatch-sync [::gym.theme/initialize])
  (reg-auth0-cofx (create-auth0-client))
  (when cfg/sentry-dsn (dispatch [::gym.error-reporting/init!]))
  (clear-subscription-cache!)
  (router/start!)
  (mount-root))

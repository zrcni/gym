(ns gym.frontend.core
  (:require
   [gym.frontend.config :as cfg]
   [gym.frontend.error-reporting]
   [gym.frontend.effects]
   [gym.frontend.events]
   [gym.frontend.subs]
   [gym.frontend.theme]
   [gym.frontend.styles :refer [inject-global-styles]]
   [gym.frontend.auth :refer [create-auth0-client reg-auth0-cofx]]
   [reagent.dom :as reagent-dom]
   [re-frame.core :refer [dispatch dispatch-sync clear-subscription-cache!]]
   [gym.frontend.router :as router]))

(defn mount-root []
  (inject-global-styles)
  (reagent-dom/render [router/root] (.getElementById js/document "app")))

(defn init! []
  (dispatch-sync [:initialize-db])
  (dispatch-sync [::gym.frontend.theme/initialize])
  (reg-auth0-cofx (create-auth0-client))
  (when cfg/sentry-dsn (dispatch [::gym.frontend.error-reporting/init!]))
  (clear-subscription-cache!)
  (router/start!)
  (mount-root))

(ns gym.core
  (:require
   [gym.effects]
   [gym.events]
   [gym.subs]
   [gym.styles :refer [inject-global-styles]]
   [gym.auth :refer [create-auth0-client reg-auth0-cofx]]
   [reagent.core :as reagent]
   [re-frame.core :refer [dispatch-sync clear-subscription-cache!]]
   [gym.router :as router]))

(defn mount-root []
  (inject-global-styles)
  (reagent/render [router/root] (.getElementById js/document "app")))

(defn init! []
  (reg-auth0-cofx (create-auth0-client))
  (dispatch-sync [:initialize-db])
  (clear-subscription-cache!)
  (router/start!)
  (mount-root))

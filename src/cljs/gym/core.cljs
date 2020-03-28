(ns gym.core
  (:require
   [gym.auth :refer [init-firebase-app start-firebase-auth-state-listener]]
   [reagent.core :as reagent]
   [re-frame.core :refer [dispatch-sync clear-subscription-cache!]]
   [gym.router :as router]))

(defn mount-root []
  (reagent/render [router/root] (.getElementById js/document "app")))

(defn init! []
  (dispatch-sync [:initialize-db])
  (clear-subscription-cache!)
  (router/start!)
  (init-firebase-app)
  (mount-root)
  (start-firebase-auth-state-listener))

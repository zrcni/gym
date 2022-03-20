(ns gym.frontend.router
  (:require
   [reagent.core :as reagent]
   [clerk.core :as clerk]
   [gym.frontend.components.loaders :as loaders]
   [gym.frontend.views :refer [layout]]
   [gym.frontend.analytics.routes :as analytics]
   [gym.frontend.home.routes :as home]
   [gym.frontend.login.routes :as login]
   [gym.frontend.settings.routes :as settings]
   [gym.frontend.login.subs]
   [gym.frontend.login-callback.routes :as login-callback]
   [cljss.core :refer-macros [defstyles]]
   [reitit.frontend.easy :as rfe]
   [reitit.frontend :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.coercion.spec :as rss]
   [re-frame.core :refer [reg-event-db reg-event-fx dispatch subscribe]]))

;; register re-frame effects etc.
(reg-event-fx :navigate
              (fn [_ [_ & route]]
                {:navigate! route}))

(reg-event-db :navigated
              (fn [db [_ new-match]]
                (assoc db :current-route new-match)))

(defonce routes
  ["/"
   home/routes
   login/routes
   login-callback/routes
   settings/routes
   analytics/routes])

(def router
  (rf/router
   routes
   {:data {:coercion rss/coercion}
    :conflicts nil}))

(defn current-page [{:keys [route]}]
  (let [view (-> route :data :view)
        name (-> route :data :name)
        wrapper (-> route :data :wrapper)]
    (if-not (nil? wrapper)
      [wrapper
       [view {:key name}]]
      [view {:key name}])))

(defstyles loader-wrapper-style []
  {:margin-top "1rem"
   :justify-content "center"
   :display "flex"
   :flex-direction "column"
   :align-items "center"})

(defn root []
  (dispatch [:handle-first-load])
  (fn []
    (let [auth-status @(subscribe [:gym.frontend.login.subs/auth-status])
          current-route @(subscribe [:current-route])]
      [layout {:disabled (= auth-status :waiting)}
       (if (= auth-status :waiting)
         [:div {:key current-page
                :class (loader-wrapper-style)}
          [loaders/circle {:size 160}]]

         (when current-route
           [current-page {:key (:path current-route)
                          :route current-route}]))])))

(defn on-navigate [new-match]
  (let [old-match (subscribe [:current-route])]
    (when new-match
      (let [cs (rfc/apply-controllers (:controllers @old-match) new-match)
            m  (assoc new-match :controllers cs)]
        (dispatch [:navigated m])
        (clerk/navigate-page! (:template m))))))

(defn init-routes! []
  (rfe/start!
   router
   on-navigate
   {:use-fragment false})
  (reagent/after-render clerk/after-render!))

(defn start! []
  (init-routes!))

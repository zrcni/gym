(ns gym.router
  (:require
   [reagent.core :as reagent]
   [clerk.core :as clerk]
   [gym.views :refer [layout]]
   [gym.metrics :as metrics]
   [gym.home.routes :as home]
   [gym.login.routes :as login]
   [gym.login.subs]
   [gym.login-callback.routes :as login-callback]
   [reitit.frontend.easy :as rfe]
   [reitit.frontend :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.coercion.spec :as rss]
   [re-frame.core :refer [reg-event-fx dispatch subscribe]]))

;; register re-frame effects etc.
(reg-event-fx :navigate
              (fn [_ [_ & route]]
                {:navigate! route}))

(reg-event-fx :navigated
              (fn [{:keys [db]} [_ new-match]]
                {:db (assoc db :current-route new-match)
                 :dispatch [::metrics/app-event "page-view"]}))

(defonce routes
  ["/"
   home/routes
   login/routes
   login-callback/routes])

(def router
  (rf/router
   routes
   {:data {:coercion rss/coercion}}))

(defn current-page [{:keys [route]}]
  (let [view (-> route :data :view)
        name (-> route :data :name)
        wrapper (-> route :data :wrapper)]
    (if-not (nil? wrapper)
      ^{:key name} [wrapper [view]]
      ^{:key name} [view])))

(defn root []
  (dispatch [:handle-first-load])
  (fn []
    (let [auth-status @(subscribe [:gym.login.subs/auth-status])
          current-route @(subscribe [:current-route])]
      [layout {:disabled (= auth-status :waiting)}
       (if (= auth-status :waiting)
         [:div.circle-loader]
         (when current-route
           ^{:key (:path current-route)} [current-page {:route current-route}]))])))

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

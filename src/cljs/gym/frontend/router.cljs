(ns gym.frontend.router
  (:require
   [reagent.core :as reagent]
   [clerk.core :as clerk]
   [gym.frontend.components.loaders :as loaders]
   [gym.frontend.views :refer [layout]]
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
   settings/routes])

(def router
  (rf/router
   routes
   {:data {:coercion rss/coercion}}))

(defn current-page [{:keys [route]}]
  (let [view (-> route :data :view)
        name (-> route :data :name)
        wrapper (-> route :data :wrapper)]
    (if-not (nil? wrapper)
      ^{:key name} [wrapper
                    ^{:key name}
                    [view]]
      ^{:key name} [view])))

(defstyles loader-wrapper-style []
  {:margin-top "1rem"
   :justify-content "center"
   :display "flex"
   :flex-direction "column"
   :align-items "center"})

(defstyles loader-description-style []
  {:margin-top "1rem"
   :text-align "center"})

(defn root []
  (dispatch [:handle-first-load])
  (fn []
    (let [auth-status @(subscribe [:gym.frontend.login.subs/auth-status])
          current-route @(subscribe [:current-route])]
      [layout {:disabled (= auth-status :waiting)}
       (if (= auth-status :waiting)
         ^{:key current-page}
         [:div {:class (loader-wrapper-style)}
          [loaders/circle {:size 80}]
          [:p {:class (loader-description-style)}
           "The server might be starting right now, if you're the first user in a while..."]]

         (when current-route
           ^{:key (:path current-route)}
           [current-page {:route current-route}]))])))

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

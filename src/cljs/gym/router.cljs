(ns gym.router
  (:require
   [reagent.core :as reagent]
   [clerk.core :as clerk]
   [gym.views :refer [layout]]
   [gym.views.home :refer [home-view]]
   [gym.views.login :refer [login-view]]
   [gym.views.login-callback :refer [login-callback-view]]
   [reitit.frontend.easy :as rfe]
   [reitit.frontend :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.coercion.spec :as rss]
   [re-frame.core :refer [reg-event-db reg-event-fx reg-fx dispatch subscribe]]))

;; register re-frame effects etc.
(reg-event-fx :navigate
              (fn [_ [_ & route]]
                {:navigate! route}))

(reg-fx :navigate!
        (fn [route]
          (apply rfe/push-state route)))

(reg-event-db :navigated
              (fn [db [_ new-match]]
                (assoc db :current-route new-match)))

"Navigate on render"
(defn navigate [{:keys [to]}]
  (dispatch [:navigate to])
  (fn [] nil))

"Navigate to /login if logged out"
(defn private-route []
  (let [user @(subscribe [:user])]
    (fn [& children]
      (if-not user
        [navigate {:to :login}]
        [:<> children]))))

"Navigate to / if logged in"
(defn public-route []
  (let [user @(subscribe [:user])]
    (fn [& children]
      (if user
       [navigate {:to :home}]
        [:<> children]))))

(defonce routes
  ["/"
   ["" {:name :home
        :view home-view
        :wrapper  private-route
        :title "Home"
        :controllers []}]
   ["login" {:name :login
             :view login-view
             :wrapper  public-route
             :title "Login"
             :controllers []}]
   ["auth0_callback" {:name :login-callback
                      :view login-callback-view
                      :wrapper public-route
                      :title "Logging in..."
                      :controllers []}]])

(def router
  (rf/router
   routes
   {:data {:coercion rss/coercion}}))

(defn current-page [{:keys [route]}]
  (let [view (-> route :data :view)
        name (-> route :data :name)
        wrapper (-> route :data :wrapper)]
    (if (not (nil? wrapper))
      ^{:key name} [wrapper [view]]
      ^{:key name} [view])))

(defn root []
  (dispatch [:handle-first-load])
  (fn []
    (let [login-status @(subscribe [:login-status])
          current-route @(subscribe [:current-route])]
      [layout {:disabled (= login-status "WAITING")}
       (if (= login-status "WAITING")
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

(ns gym.router
  (:require
   [reagent.core :as reagent]
   [clerk.core :as clerk]
   [gym.views :as views]
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
        :view views/home-page
        :wrapper  private-route
        :title "Home"
        :controllers []}]
    ["login" {:name :login
          :view views/login-page
          :wrapper  public-route
          :title "Login"
          :controllers []}]
   ["login_success" {:name :login-success
                     :view (fn [] [:div "Verifying login..."])
                     :wrapper  public-route
                     :title "Login succ"
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
  (let [current-route @(subscribe [:current-route])]
    [views/layout {:disabled false}
     (when current-route
       ^{:key (:path current-route)} [current-page {:route current-route}])]))

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

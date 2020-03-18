(ns react-cljs.router
  (:require
   [reagent.core :as reagent]
   [clerk.core :as clerk]
   [react-cljs.views :as views]
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

(defonce routes
  ["/"
   ["" {:name :home
        :view views/home-page
        :wrapper  nil
        :title "Home"
        :controllers []}]])

(def router
  (rf/router
   routes
   {:data {:coercion rss/coercion}}))

(defn current-page [{:keys [route]}]
  (let [view (get-in route [:data :view])
        name (get-in route [:data :name])
        wrapper (get-in route [:data :wrapper])]
    (if (not (nil? wrapper))
      [wrapper ^{:key name} [view]]
      ^{:key name} [view])))

(defn root []
  (let [current-route @(subscribe [:current-route])]
    [views/layout {:disabled false}
      (when current-route
        ^{:key (:template current-route)} [current-page {:route current-route}])]))

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

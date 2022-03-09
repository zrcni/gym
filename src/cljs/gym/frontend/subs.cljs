(ns gym.frontend.subs
  (:require
   [gym.frontend.home.subs]
   [gym.frontend.login.subs]
   [gym.frontend.analytics.subs]
   [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :current-route
 (fn [db]
   (:current-route db)))

(reg-sub
 :loading
 (fn [db [_ kw]]
   (get-in db [:loading kw])))

(reg-sub
 :error
 (fn [db _]
   (:error db)))

(reg-sub
 :user
 (fn [db _] (:user db)))

(reg-sub
 :theme
 (fn [db _]
   (:theme db)))

(reg-sub
 :user-prefs
 (fn [db _]
   (:user-prefs db)))

(reg-sub
 :excluded-tags
 (fn [_ _] (subscribe [:user-prefs]))
 (fn [user-prefs _]
   (:excluded_tags user-prefs)))

(reg-sub
 :all-tags
 (fn [db _]
   (-> db :all-tags)))

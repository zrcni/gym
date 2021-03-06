(ns gym.frontend.subs
  (:require
   [gym.frontend.home.subs]
   [gym.frontend.login.subs]
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :current-route
 (fn [db]
   (:current-route db)))

(reg-sub
 :loading  ;; usage: (subscribe [:loading])
 (fn [db _]
   (:loading db)))

(reg-sub
 :error  ;; usage: (subscribe [:error])
 (fn [db _]
   (:error db)))

(reg-sub
 :user
 (fn [db _] (:user db)))

(reg-sub
 :theme
 (fn [db _]
   (:theme db)))

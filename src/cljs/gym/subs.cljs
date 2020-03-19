(ns gym.subs
  (:require
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
 :calendar-start-date
 (fn [db _]
   (-> db :calendar :start-date)))

(reg-sub
 :calendar-editing-index
 (fn [db _]
   (-> db :calendar :editing-index)))

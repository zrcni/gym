(ns gym.home.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :calendar-start-date
 (fn [db _]
   (-> db :home :calendar :start-date)))

(reg-sub
 :calendar-editing-index
 (fn [db _]
   (-> db :home :calendar :editing-index)))

(reg-sub
 :calendar-weeks
 (fn [db _]
   (-> db :home :calendar :weeks)))

(reg-sub
 :calendar-loading
 (fn [db _]
   (-> db :home :calendar :loading)))

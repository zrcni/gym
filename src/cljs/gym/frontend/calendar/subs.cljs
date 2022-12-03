(ns gym.frontend.calendar.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :calendar-start-date
 (fn [db _]
   (-> db :calendar :start-date)))

(reg-sub
 :calendar-editing-index
 (fn [db _]
   (-> db :calendar :editing-index)))

(reg-sub
 :calendar-weeks
 (fn [db _]
   (-> db :calendar :weeks)))

(reg-sub
 :calendar-loading
 (fn [db _]
   (-> db :calendar :loading)))

(reg-sub
 :suggested-workout-tags
 (fn [db _]
   (-> db :calendar :suggested-workout-tags)))

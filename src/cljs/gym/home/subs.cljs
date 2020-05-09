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
 :current-week-exercise-duration
 (fn [db _] (-> db :home :stats :current-week-exercise-duration)))

(reg-sub
 :current-month-exercise-duration
 (fn [db _] (-> db :home :stats :current-month-exercise-duration)))

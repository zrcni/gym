(ns gym.home.duration-cards.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::week-exercise-duration
 (fn [db _]
   (-> db :home :duration-cards :week-exercise-duration)))

(reg-sub
 ::month-exercise-duration
 (fn [db _]
   (-> db :home :duration-cards :month-exercise-duration)))

(reg-sub
 ::exercise-duration-loading
 (fn [db _]
   (-> db :home :duration-cards :loading)))

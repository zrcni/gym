(ns gym.effects
  (:require
   [toastr]
   [reitit.frontend.easy :as rfe]
   [re-frame.core :refer [reg-fx]]))

(reg-fx :navigate!
        (fn [route]
          (apply rfe/push-state route)))

(reg-fx :toast-success!
        (fn [message]
          (.success toastr message)))

(reg-fx :toast-error!
        (fn [message]
          (.error toastr message)))

(ns gym.events
  (:require
   [gym.db :refer [default-db]]
   [day8.re-frame.http-fx]
   ["toastr" :as toastr]
   [cljs-time.core :as t]
   [gym.calendar-utils :refer [add-time subtract-time]]
   [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]))

(reg-event-db :initialize-db
 (fn [_ _] default-db))

(reg-fx :set-local-storage!
  (fn [_ [key value]]
    (js/localStorage.setItem key value)))

(reg-event-fx :set-local-storage
  (fn [_ params]
    {:set-local-storage! params}))

(reg-fx :remove-local-storage!
  (fn [_ key]
    (js/localStorage.removeItem key)))

(reg-event-fx :remove-local-storage
  (fn [_ params]
    {:remove-local-storage! params}))

(reg-fx :toast-success!
  (fn [_ message]
    (toastr/success message)))

(reg-fx :toast-error!
  (fn [_ message]
    (toastr/error message)))

(reg-event-db :calendar-show-later
  (fn [db [_ time]]
    (let [start-date (-> db :calendar :start-date)]
      (assoc-in db [:calendar :start-date] (add-time start-date time)))))

(reg-event-db :calendar-show-earlier
  (fn [db [_ time]]
    (let [start-date (-> db :calendar :start-date)]
      (assoc-in db [:calendar :start-date] (subtract-time start-date time)))))

(reg-event-db :calendar-edit-day
  (fn [db [_ day-index]]
    (assoc-in db [:calendar :editing-index] day-index)))

(reg-event-db :calendar-stop-editing
  (fn [db]
    (assoc-in db [:calendar :editing-index] nil)))

;; this is used for http requests that don't need failure or success handlers
(reg-event-fx :no-op (fn []))

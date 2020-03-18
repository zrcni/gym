(ns react-cljs.events
  (:require
   [react-cljs.db :refer [default-db]]
   [day8.re-frame.http-fx]
   ["toastr" :as toastr]
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

;; this is used for http requests that don't need failure or success handlers
(reg-event-fx :no-op (fn []))

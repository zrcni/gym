(ns gym.events
  (:require
   [gym.db :refer [default-db]]
   [day8.re-frame.http-fx]
   ["toastr" :as toastr]
   [gym.calendar-utils :refer [add-time subtract-time]]
   [ajax.core :refer [text-request-format json-request-format json-response-format]]
   [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]))

(def ^:private api-url (atom "http://localhost:3001/api"))

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

(reg-event-db :calendar-update-workouts
  (fn [db [_ workouts]]
    (assoc-in db [:calendar :workouts] workouts)))

(reg-event-fx :fetch-all-workouts
              (fn [_ _]
                {:http-xhrio {:method :get
                              :uri (str @api-url "/workouts")
                              :format (text-request-format)
                              :response-format (json-response-format {:keywords? true})
                              :on-success [:calendar-update-workouts]
                              :on-failure [:no-op]}}))

;; this is used for http requests that don't need failure or success handlers
(reg-event-fx :no-op (fn []))

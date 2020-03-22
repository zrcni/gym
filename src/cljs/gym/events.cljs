(ns gym.events
  (:require
   [gym.db :refer [default-db]]
   [day8.re-frame.http-fx]
   ["toastr" :as toastr]
   [gym.calendar-utils :refer [calculate-weeks add-duration subtract-duration]]
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

(reg-event-db :calendar-update-start-date
  (fn [db [_ date]]
    (assoc-in db [:calendar :start-date] date)))

;; reuse logic for updating start date when adding/subtracting a duration from the date
(defn reg-start-date-update-event-fx
  "f is expected to be a function that takes and returns a date"
  [fx-name f]
  (reg-event-fx fx-name
    (fn [{:keys [db]} [_ duration]]
      (let [start-date (get-in db [:calendar :start-date])
            next-date (f start-date duration)]
        {:dispatch-n [[:calendar-update-start-date next-date]
                      [:calendar-update-weeks]]}))))

(reg-start-date-update-event-fx :calendar-show-earlier subtract-duration)
(reg-start-date-update-event-fx :calendar-show-later add-duration)

(reg-event-db :calendar-update-weeks
  (fn [db]
    (let [start-date (get-in db [:calendar :start-date])
          workouts (get-in db [:calendar :workouts])
          weeks (calculate-weeks start-date workouts)]
      (assoc-in db [:calendar :weeks] weeks))))

(reg-event-db :calendar-edit-day
  (fn [db [_ day-index]]
    (assoc-in db [:calendar :editing-index] day-index)))

(reg-event-db :calendar-stop-editing
  (fn [db]
    (assoc-in db [:calendar :editing-index] nil)))

(reg-event-db :calendar-update-workouts
  (fn [db [_ workouts]]
    (assoc-in db [:calendar :workouts] workouts)))

(reg-event-fx :fetch-all-workouts-success
  (fn [_ [_ workouts]]
    {:dispatch-n [[:calendar-update-workouts workouts]
                  [:calendar-update-weeks]]}))

(reg-event-fx :fetch-all-workouts
  (fn [_ _]
    {:http-xhrio {:method :get
                  :uri (str @api-url "/workouts")
                  :format (text-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [:fetch-all-workouts-success]
                  :on-failure [:no-op]}}))

;; this is used for http requests that don't need failure or success handlers
(reg-event-fx :no-op (fn []))

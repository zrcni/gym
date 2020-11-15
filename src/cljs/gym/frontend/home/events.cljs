(ns gym.frontend.home.events
  (:require [gym.workout :refer [validate-workout-new make-workout-new]]
            [gym.frontend.config :as cfg]
            [goog.string :as gstring]
            [goog.string.format]
            [day8.re-frame.http-fx]
            [gym.frontend.login.events]
            [gym.frontend.calendar-utils :refer [calculate-weeks add-duration subtract-duration]]
            [ajax.core :refer [json-request-format]]
            [gym.frontend.home.duration-cards.events :as duration-cards-events]
            [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-db :calendar-update-start-date
              (fn [db [_ date]]
                (assoc-in db [:home :calendar :start-date] date)))

;; reuse logic for updating start date when adding/subtracting a duration from the date
(defn reg-start-date-update-event-fx
  "f is expected to be a function that takes and returns a date"
  [fx-name f]
  (reg-event-fx fx-name
                (fn [{:keys [db]} [_ duration]]
                  (let [start-date (get-in db [:home :calendar :start-date])
                        next-date (f start-date duration)]
                    {:dispatch-n [[:calendar-update-start-date next-date]
                                  [:calendar-update-weeks]]}))))

(reg-start-date-update-event-fx :calendar-show-earlier subtract-duration)
(reg-start-date-update-event-fx :calendar-show-later add-duration)

(reg-event-db :calendar-update-weeks
              (fn [db]
                (let [start-date (get-in db [:home :calendar :start-date])
                      workouts (get-in db [:home :calendar :workouts])
                      weeks (calculate-weeks start-date workouts)]
                  (assoc-in db [:home :calendar :weeks] weeks))))

(reg-event-db :calendar-edit-day
              (fn [db [_ day-index]]
                (assoc-in db [:home :calendar :editing-index] day-index)))

(reg-event-db :calendar-stop-editing
              (fn [db]
                (assoc-in db [:home :calendar :editing-index] nil)))

(reg-event-db :calendar-update-workouts
              (fn [db [_ workouts]]
                (assoc-in db [:home :calendar :workouts] workouts)))

(reg-event-fx :fetch-all-workouts-success
              (fn [{:keys [db]} [_ workouts]]
                {:db (assoc-in db [:home :calendar :loading] false)
                 :dispatch-n [[:calendar-update-workouts workouts]
                              [:calendar-update-weeks]]}))

(reg-event-fx :fetch-all-workouts-failure
              (fn [{:keys [db]} _]
                {:db (assoc-in db [:home :calendar :loading] false)}))

(reg-event-fx :fetch-all-workouts
              (fn [{:keys [db]} _]
                {:db (assoc-in db [:home :calendar :loading] true)
                 :dispatch [:fetch {:method :get
                                    :uri (str cfg/api-url "/api/workouts")
                                    :on-success [:fetch-all-workouts-success]
                                    :on-failure [:fetch-all-workouts-failure]}]}))

(reg-event-fx :create-workout-success
              (fn [{:keys [db]} [_ workout]]
                {:db (update-in db [:home :calendar :workouts] conj workout)
                 :dispatch-n [[:calendar-update-weeks]
                              [::duration-cards-events/fetch-current-week-exercise-duration]
                              [::duration-cards-events/fetch-current-month-exercise-duration]]}))

(reg-event-fx :create-workout-request
              (fn [_ [_ workout]]
                (let [workout-new  (make-workout-new workout)]
                  (if-let [invalid-keys (validate-workout-new workout-new)]                    
                    {:toast-error! (gstring/format "%s is invalid" (first invalid-keys))}

                    {:dispatch [:fetch {:method :post
                                        :params workout-new
                                        :uri (str cfg/api-url "/api/workouts")
                                        :format (json-request-format)
                                        :on-success [:create-workout-success]}]}))))

(reg-event-fx :delete-workout-success
              (fn [{:keys [db]} [_ workout-id]]
                {:db (assoc-in
                      db
                      [:home :calendar :workouts]
                      (->> (-> db :home :calendar :workouts)
                           (filter #(not= workout-id (:workout_id %)))))
                 :dispatch-n [[:calendar-update-weeks]
                              [::duration-cards-events/fetch-current-week-exercise-duration]
                              [::duration-cards-events/fetch-current-month-exercise-duration]]}))

(reg-event-fx :delete-workout
              (fn [_ [_ workout-id]]
                {:dispatch [:fetch {:method :delete
                                    :uri (str cfg/api-url "/api/workouts/" workout-id)
                                    :on-success [:delete-workout-success workout-id]}]}))

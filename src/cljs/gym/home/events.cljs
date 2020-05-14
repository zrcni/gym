(ns gym.home.events
  (:require
   [clojure.spec.alpha :as spec]
   [gym.specs]
   [gym.metrics :as metrics]
   [gym.config :as cfg]
   [goog.string :as gstring]
   [goog.string.format]
   [day8.re-frame.http-fx]
   [gym.login.events]
   [gym.calendar-utils :refer [calculate-weeks add-duration subtract-duration]]
   [ajax.core :refer [json-request-format]]
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

(reg-event-fx :calendar-edit-day
              (fn [{:keys [db]} [_ index local-date]]
                {:db (assoc-in db [:home :calendar :editing-index] index)
                 :dispatch [::metrics/user-event "calendar-edit-day" {:date local-date}]}))

(reg-event-db :calendar-stop-editing
              (fn [db]
                (assoc-in db [:home :calendar :editing-index] nil)))

(reg-event-db :calendar-update-workouts
              (fn [db [_ workouts]]
                (assoc-in db [:home :calendar :workouts] workouts)))

(reg-event-fx :fetch-current-week-exercise-duration
              (fn [_ _]
                {:dispatch [:fetch {:method :get
                                    :uri (str cfg/api-url "/api/stats/exercises/week")
                                    :on-success [:fetch-current-week-exercise-duration-success]}]}))

(reg-event-fx :fetch-current-week-exercise-duration-success
              (fn [{:keys [db]} [_ result]]
                {:db (assoc-in db [:home :stats :current-week-exercise-duration] (:duration result))}))

(reg-event-fx :fetch-current-month-exercise-duration
              (fn [_ _]
                {:dispatch [:fetch {:method :get
                                    :uri (str cfg/api-url "/api/stats/exercises/month")
                                    :on-success [:fetch-current-month-exercise-duration-success]}]}))

(reg-event-fx :fetch-current-month-exercise-duration-success
              (fn [{:keys [db]} [_ result]]
                {:db (assoc-in db [:home :stats :current-month-exercise-duration] (:duration result))}))

(reg-event-fx :fetch-all-workouts-success
              (fn [_ [_ workouts]]
                {:dispatch-n [[:calendar-update-workouts workouts]
                              [:calendar-update-weeks]]}))

(reg-event-fx :fetch-all-workouts
              (fn [_ _]
                {:dispatch [:fetch {:method :get
                                    :uri (str cfg/api-url "/api/workouts")
                                    :on-success [:fetch-all-workouts-success]}]}))

(reg-event-fx :create-workout-success
              (fn [{:keys [db]} [_ workout]]
                {:db (update-in db [:home :calendar :workouts] conj workout)
                 :dispatch-n [[:calendar-update-weeks]
                              [:fetch-current-week-exercise-duration]
                              [:fetch-current-month-exercise-duration]]}))

(reg-event-fx :create-workout-request
              (fn [_ [_ workout]]
                (let [result (spec/explain-data :gym.specs/workout-new workout)]
                  (if result
                    (let [problems (:cljs.spec.alpha/problems result)
                          keys (mapcat #(as-> % v (:in v) (map name v)) problems)]
                      {:toast-error! (gstring/format "%s is invalid" (first keys))})

                    {:dispatch [:fetch {:method :post
                                        :params workout
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
                              [:fetch-current-week-exercise-duration]
                              [:fetch-current-month-exercise-duration]]}))

(reg-event-fx :delete-workout
              (fn [_ [_ workout-id]]
                {:dispatch [:fetch {:method :delete
                                    :uri (str cfg/api-url "/api/workouts/" workout-id)
                                    :on-success [:delete-workout-success workout-id]}]}))

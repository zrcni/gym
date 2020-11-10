(ns gym.home.duration-cards.events
  (:require
   [gym.config :as cfg]
   [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx ::fetch-current-week-exercise-duration
              (fn [{:keys [db]} _]
                {:db (assoc-in db [:home :duration-cards :loading] true)
                 :dispatch [:fetch {:method :get
                                    :uri (str cfg/api-url "/api/workouts/duration/week")
                                    :on-success [::fetch-current-week-exercise-duration-success]
                                    :on-failure [::fetch-exercise-duration-failure]}]}))

(reg-event-fx ::fetch-current-week-exercise-duration-success
              (fn [{:keys [db]} [_ result]]
                {:db (-> db
                         (assoc-in [:home :duration-cards :loading] false)
                         (assoc-in [:home :duration-cards :week-exercise-duration] (:duration result)))}))

(reg-event-fx ::fetch-current-month-exercise-duration
              (fn [_ _]
                {:dispatch [:fetch {:method :get
                                    :uri (str cfg/api-url "/api/workouts/duration/month")
                                    :on-success [::fetch-current-month-exercise-duration-success]
                                    :on-failure [::fetch-exercise-duration-failure]}]}))

(reg-event-fx ::fetch-current-month-exercise-duration-success
              (fn [{:keys [db]} [_ result]]
                {:db (-> db
                         (assoc-in [:home :duration-cards :loading] false)
                         (assoc-in [:home :duration-cards :month-exercise-duration] (:duration result)))}))

(reg-event-fx ::fetch-exercise-duration-failure
              (fn [{:keys [db]} _]
                {:db (assoc-in db [:home :duration-cards :loading] false)}))

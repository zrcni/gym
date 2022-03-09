(ns gym.frontend.settings.events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [ajax.core :refer [json-request-format]]
            [gym.frontend.config :as cfg]
            [gym.frontend.theme]))

(reg-event-fx
 :save-user-prefs
 (fn [{:keys [db]} _]
   (let [theme-color (-> db :theme :theme-color)
         excluded-tags (-> db :user-prefs :excluded_tags)]
     {:dispatch [:fetch {:method :put
                         :uri (str cfg/api-url "/api/users/preferences")
                         :params {:theme_main_color theme-color
                                  :excluded_tags excluded-tags}
                         :format (json-request-format)
                         :on-success [:save-user-prefs-succeeded]
                         :on-failure [:save-user-prefs-failed]}]})))


(reg-event-fx
 :save-user-prefs-succeeded
 (fn [_ [_ prefs]]
   {:dispatch-n [[:update-user-prefs prefs]
                 [::gym.frontend.theme/persist-theme]]}))

(reg-event-fx
 :save-user-prefs-failed
 (fn [_ _]
   {}))

(reg-event-fx
 :toggle-excluded-tag
 (fn [{:keys [db]} [_ tag]]
   (if (some #(= % tag) (:all-tags db))
     (let [prefs (:user-prefs db)
           prefs (if (some #(= % tag) (:excluded_tags prefs))
                   (update prefs :excluded_tags (fn [tags]
                                                  (into [] (remove #(= % tag) tags))))
                   (update prefs :excluded_tags conj tag))]
       {:dispatch [:update-user-prefs prefs]})
     {})))

(reg-event-fx
 :fetch-all-workout-tags
 (fn [_ _]
   {:dispatch [:fetch {:method :get
                       :uri (str cfg/api-url "/api/workout_tags")
                       :on-success [:fetch-all-workout-tags-succeeded]
                       :on-failure [:fetch-all-workout-tags-failed]}]}))

(reg-event-fx
 :fetch-all-workout-tags-succeeded
 (fn [_ [_ tags]]
   {:dispatch [:set-all-tags tags]}))

(reg-event-fx
 :fetch-all-workout-tags-failed
 (fn [_ _]
   {}))

(reg-event-db
 :set-all-tags
 (fn [db [_ tags]]
   (assoc db :all-tags tags)))

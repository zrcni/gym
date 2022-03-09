(ns gym.frontend.settings.events
  (:require [re-frame.core :refer [reg-event-fx]]
            [ajax.core :refer [json-request-format]]
            [gym.frontend.config :as cfg]
            [gym.frontend.theme]))

(reg-event-fx
 :fetch-user-prefs
 (fn [_ [_ {:keys [on-success on-failure]}]]
   {:dispatch [:fetch {:method :get
                       :uri (str cfg/api-url "/api/users/preferences")
                       :on-success on-success
                       :on-failure on-failure}]}))

(reg-event-fx
 :save-user-prefs
 (fn [{:keys [db]} _]
   (let [theme-color (-> db :theme :theme-color)
         excluded-tags []]
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
 (fn [_ _]))

(comment
  (require '[re-frame.core :as rf])
  (rf/dispatch [:fetch-user-prefs])
)

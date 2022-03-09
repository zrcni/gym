(ns gym.frontend.events
  (:require
   [gym.frontend.db :refer [default-db]]
   [goog.string.format]
   [day8.re-frame.http-fx]
   [gym.frontend.login.events]
   [gym.frontend.home.events]
   [gym.frontend.settings.events]
   [gym.frontend.analytics.events]
   [parse-color]
   [gym.frontend.config :as cfg]
   [ajax.core :refer [text-request-format json-response-format]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-db
 :start-loading
 (fn [db [_ kw]]
   (assoc-in db [:loading kw] true)))

(reg-event-db
 :stop-loading
 (fn [db [_ kw]]
   (update db :loading dissoc kw)))

(reg-event-db :initialize-db
              (fn [_ _] default-db))

(reg-event-fx :handle-first-load
              (fn [{:keys [db]} [_ _]]
                (if (re-matches #"^/auth0_callback" (-> js/window .-location .-pathname))
                  {:db (assoc db :auth-status :login-callback)}
                  {:dispatch [:gym.frontend.login.events/verify-auth]})))

(defn make-default-fetch-params [cofx]
  {:headers {:authorization (str "Bearer " (-> cofx :db :token))}
   :format (text-request-format)
   :response-format (json-response-format {:keywords? true})
   :on-failure [:on-request-failure]})

(reg-event-fx :fetch
              (fn [cofx [_ params]]
                {:http-xhrio (merge (make-default-fetch-params cofx)
                                    params)}))

(reg-event-fx :handle-unauthorized-request
              (fn [_ [_ error]]
                {:toast-error! error
                 :dispatch [:gym.frontend.login.events/logout]}))

(reg-event-fx :handle-request-error
              (fn [_ [_ error]]
                {:toast-error! error}))

(reg-event-fx :on-request-failure []
              (fn [_ [_ response]]
                (let [error (-> response :response :error)]
                  (if (= 401 (-> response :status))
                    {:dispatch [:handle-unauthorized-request error]}
                    {:dispatch [:handle-request-error error]}))))

(reg-event-db
 :update-user-prefs
 (fn [db [_ prefs]]
   (assoc db :user-prefs prefs)))

(reg-event-fx
 :fetch-user-prefs
 (fn [_ [_ {:keys [on-success on-failure]}]]
   {:dispatch [:fetch {:method :get
                       :uri (str cfg/api-url "/api/users/preferences")
                       :on-success on-success
                       :on-failure on-failure}]}))

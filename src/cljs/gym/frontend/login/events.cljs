(ns gym.frontend.login.events
  (:require
   [gym.frontend.error-reporting]
   [gym.frontend.config :as cfg]
   [gym.frontend.theme]
   [re-frame.core :refer [reg-event-fx inject-cofx dispatch]]))

(reg-event-fx ::verify-auth
              (inject-cofx :auth0)
              (fn [cofx _]
                (-> (.getTokenSilently (:auth0 cofx) (clj->js {:audience "exercise-tracker-api"}))
                    (.then #(dispatch [::verify-auth-success]))
                    (.catch #(dispatch [::verify-auth-failure (js->clj (get (js->clj %) "error"))])))
                {}))

(reg-event-fx ::verify-auth-success
              (fn [_ [_ _]]
                {:dispatch [::handle-login-auth0-success]}))

(reg-event-fx ::verify-auth-failure
              (fn [_ [_ _error]]
                {:dispatch [::logout-auth0-success]}))


(reg-event-fx
 ::login-success
 (fn [{:keys [db]} [_ body]]
   (cond-> {}
     true
     (assoc :db (-> db
                    (assoc :user (:user body))
                    (assoc :auth-status :logged-in)))

     true
     (assoc :dispatch-n [[:fetch-user-prefs {:on-success [:fetch-user-prefs-after-login-success]
                                             :on-failure [:fetch-user-prefs-after-login-failure]}]
                         [:start-loading :fetch-initial-user-prefs]])

     (re-matches #"^(/login|/auth0_callback)" (-> js/window .-location .-pathname))
     (assoc :navigate! [:home])

     cfg/sentry-dsn
     (assoc ::gym.frontend.error-reporting/set-sentry-user-info! {:id (:user_id (:user body))}))))


(reg-event-fx
 :fetch-user-prefs-after-login-success
 (fn [_ [_ prefs]]
   (cond-> {}
     true
     (update :dispatch-n conj [:update-user-prefs prefs])

     true
     (update :dispatch-n conj [:stop-loading :fetch-initial-user-prefs])

     (:theme_main_color prefs)
     (update :dispatch-n conj [::gym.frontend.theme/set-theme-color (:theme_main_color prefs)]))))

;; TODO: handle errors
(reg-event-fx
 :fetch-user-prefs-after-login-failure
 (fn [_ _]
   {:dispatch [:stop-loading :fetch-initial-user-prefs]}))

(reg-event-fx ::login-failure
              (fn [_ [_ error]]
                {:toast-error! error
                 :dispatch [::logout-auth0]}))

(reg-event-fx ::login-auth0-success
              (fn [{:keys [db]} [_ token]]
                {:db (assoc db :token token)
                 :dispatch [::login token]}))

(reg-event-fx ::login-auth0-failure
              (fn [_ [_ error]]
                {:toast-error! error
                 :dispatch [::logout-auth0]}))

(reg-event-fx ::login
              (fn [_ _]
                {:dispatch [:fetch {:method :post
                                    :uri (str cfg/api-url "/api/auth/login")
                                    :on-success [::login-success]
                                    :on-failure [::login-failure]}]}))

(reg-event-fx ::logout
              (fn [_ _]
                {:dispatch [::logout-auth0]}))

(reg-event-fx ::login-auth0
              (inject-cofx :auth0)
              (fn [cofx [_ _]]
                (.loginWithRedirect (:auth0 cofx) (clj->js {:audience "exercise-tracker-api"}))
                {}))

(reg-event-fx ::logout-auth0
              (inject-cofx :auth0)
              (fn [cofx [_ _]]
                (.logout (:auth0 cofx) (clj->js {:localOnly true}))
                {:dispatch [::logout-auth0-success]}))

(reg-event-fx ::handle-login-auth0-success
              (inject-cofx :auth0)
              (fn [cofx [_ _]]
                (-> (.getTokenSilently (:auth0 cofx) (clj->js {:audience "exercise-tracker-api"}))
                    (.then #(dispatch [::login-auth0-success %]))
                    (.catch #(dispatch [::login-auth0-failure (get (js->clj %) "error")])))
                {}))

(reg-event-fx ::handle-login-auth0-callback
              (inject-cofx :auth0)
              (fn [cofx [_ _]]
                (-> (-> cofx :auth0 .handleRedirectCallback)
                    (.then #(dispatch [::handle-login-auth0-success]))
                    (.catch #(dispatch [::login-auth0-failure %])))
                {}))

(reg-event-fx ::logout-auth0-success
              (fn [{:keys [db]} [_ _]]
                ;; NOTE: there's probably a better way to conditionally assoc to map and re-bind to a symbol
                (as-> {} events
                  (assoc events :db (-> db
                                        (assoc :user nil)
                                        (assoc :token nil)
                                        (assoc :auth-status :logged-out)))
                  (if-not (= (-> js/window .-location .-pathname) "/login")
                    (assoc events :navigate! [:login])
                    events)
                  (if cfg/sentry-dsn
                    (assoc events ::gym.frontend.error-reporting/set-sentry-user-info! nil)
                    events))))

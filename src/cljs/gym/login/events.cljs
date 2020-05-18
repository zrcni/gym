(ns gym.login.events
  (:require
   [gym.config :as cfg]
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

(reg-event-fx ::login-success
              (fn [{:keys [db]} [_ body]]
                (let [events {:db (-> db
                                      (assoc :user (:user body))
                                      (assoc :auth-status :logged-in))}]
                  (when (re-matches #"^(/login|/auth0_callback)" (-> js/window .-location .-pathname))
                    (assoc events :navigate! [:home]))
                  (when cfg/sentry-dsn (assoc events :set-sentry-user-info! {:id (:user_id (:user body))}))
                  events)))

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
                (let [events {:db (-> db
                                      (assoc :user nil)
                                      (assoc :token nil)
                                      (assoc :auth-status :logged-out))}]
                  (when-not (= (-> js/window .-location .-pathname) "/login")
                    (assoc events :navigate! [:login]))
                  (when cfg/sentry-dsn (assoc events :set-sentry-user-info! nil))
                  events)))

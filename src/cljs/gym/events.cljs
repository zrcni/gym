(ns gym.events
  (:require
   [clojure.spec.alpha :as spec]
   [gym.db :refer [default-db]]
   [gym.specs]
   [gym.config :as cfg]
   [goog.string :as gstring]
   [goog.string.format]
   [day8.re-frame.http-fx]
   [toastr]
   [gym.auth :refer [auth0->user]]
   [gym.calendar-utils :refer [calculate-weeks add-duration subtract-duration]]
   [ajax.core :refer [text-request-format json-request-format json-response-format]]
   [re-frame.core :refer [reg-event-db reg-event-fx reg-fx inject-cofx dispatch]]))

(reg-event-db :initialize-db
 (fn [_ _] default-db))

(reg-fx :set-local-storage!
  (fn [key value]
    (js/localStorage.setItem key value)))

(reg-event-fx :set-local-storage
  (fn [_ [_ params]]
    {:set-local-storage! params}))

(reg-fx :remove-local-storage!
  (fn [key]
    (js/localStorage.removeItem key)))

(reg-event-fx :remove-local-storage
  (fn [_ [_ params]]
    {:remove-local-storage! params}))

(reg-fx :toast-success!
  (fn [message]
    (.success js/toastr message)))

(reg-fx :toast-error!
  (fn [message]
    (.error js/toastr message)))

(reg-event-db :calendar-update-start-date
  (fn [db [_ date]]
    (assoc-in db [:calendar :start-date] date)))

(defn make-default-fetch-params [cofx]
  {:headers {:authorization (str "Bearer " (-> cofx :db :token))}
   :format (text-request-format)
   :response-format (json-response-format {:keywords? true})
   :on-failure [:on-request-failure]})

(reg-event-fx :fetch
  (fn [cofx [_ params]]
    {:http-xhrio (merge-with into
                             (make-default-fetch-params cofx)
                             params)}))

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
    {:dispatch [:fetch {:method :get
                        :uri (str cfg/api-url "/api/workouts")
                        :on-success [:fetch-all-workouts-success]}]}))

(reg-event-fx :create-workout-success
  (fn [{:keys [db]} [_ workout]]
    {:db (update-in db [:calendar :workouts] conj workout)
     :dispatch [:calendar-update-weeks]}))

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
          [:calendar :workouts]
          (->> (-> db :calendar :workouts)
               (filter #(not= workout-id (:workout_id %)))))
     :dispatch [:calendar-update-weeks]}))

(reg-event-fx :delete-workout
  (fn [_ [_ workout-id]]
    {:dispatch [:fetch {:method :delete
                        :uri (str cfg/api-url "/api/workouts/" workout-id)
                        :on-success [:delete-workout-success workout-id]}]}))

(reg-event-fx :verify-authentication
              (inject-cofx :auth0)
              (fn [cofx _]
                (-> (.getTokenSilently (:auth0 cofx) (clj->js {:audience "exercise-tracker-api"}))
                    (.then #(dispatch [:verify-authentication-success]))
                    (.catch #(dispatch [:verify-authentication-failure (js->clj (get (js->clj %) "error"))])))
                {}))

(reg-event-fx :verify-authentication-success
  (fn [_ [_ _]]
    {:dispatch [:handle-login-success]}))

(reg-event-fx :verify-authentication-failure
  (fn [_ [_ _error]]
    {:dispatch [:logout-success]}))

(reg-event-fx :handle-first-load
              (fn [{:keys [db]} [_ _]]
                (if (re-matches #"^/auth0_callback" (-> js/window .-location .-pathname))
                  {:db (assoc db :login-status "LOGIN_CALLBACK")}
                  {:dispatch [:verify-authentication]})))

(reg-event-fx :login-success
  (fn [{:keys [db]} [_ user token]]
    (let [events {:db (-> db
                          (assoc :user user)
                          (assoc :token token)
                          (assoc :login-status "LOGGED_IN"))}]
      (if (re-matches #"^(/login|/auth0_callback)" (-> js/window .-location .-pathname))
       (assoc events :navigate! [:home])
        events))))

(reg-event-fx :login-failure
  (fn [_ [_ error]]
    {:toast-error! error
     :dispatch [:logout]}))

(reg-event-fx :login
              (inject-cofx :auth0)
              (fn [cofx [_ _]]
                (.loginWithRedirect (:auth0 cofx) (clj->js {:audience "exercise-tracker-api"}))
                {}))

(reg-event-fx :logout
              (inject-cofx :auth0)
              (fn [cofx [_ _]]
                (.logout (:auth0 cofx) (clj->js {:localOnly true}))
                {:dispatch [:logout-success]}))

(reg-event-fx :handle-login-success
              (inject-cofx :auth0)
              (fn [cofx [_ _]]
                (-> (js/Promise.all
                     [(.getTokenSilently (:auth0 cofx) (clj->js {:audience "exercise-tracker-api"}))
                      (.getUser (:auth0 cofx) {:audience "exercise-tracker-api"})])
                    (.then (fn [[token user]]
                             (dispatch [:login-success (auth0->user user) token])))
                    (.catch #(dispatch [:login-failure (get (js->clj %) "error")])))
                {}))

(reg-event-fx :handle-login-callback
              (inject-cofx :auth0)
              (fn [cofx [_ _]]
                (-> (-> cofx :auth0 .handleRedirectCallback)
                  (.then #(dispatch [:handle-login-success]))
                  (.catch #(dispatch [:login-failure %])))
                {}))

(reg-event-fx :logout-success
  (fn [{:keys [db]} [_ _]]
    (let [events {:db (-> db
                          (assoc :user nil)
                          (assoc :token nil)
                          (assoc :login-status "LOGGED_OUT"))}]
      (if-not (= (-> js/window .-location .-pathname) "/login")
        (assoc events :navigate! [:login])
        events))))

(reg-event-fx :handle-unauthorized-request
  (fn [_ [_ error]]
    {:toast-error! error
     :dispatch [:logout]}))

(reg-event-fx :handle-request-error
  (fn [_ [_ error]]
    {:toast-error! error}))

(reg-event-fx :on-request-failure []
  (fn [_ [_ response]]
    ;; why does the response have a :response key?
    (let [error (-> response :response :error)]
      (if (= 401 (-> response :status))
        {:dispatch [:handle-unauthorized-request error]}
        {:dispatch [:handle-request-error error]}))))

;; this is used for http requests that don't need failure or success handlers
(reg-event-fx :no-op (fn [] nil))

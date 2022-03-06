(ns gym.frontend.events
  (:require
   [gym.frontend.db :refer [default-db]]
   [goog.string.format]
   [day8.re-frame.http-fx]
   [gym.frontend.login.events]
   [gym.frontend.home.events]
   [gym.frontend.analytics.events]
   [parse-color]
   [ajax.core :refer [text-request-format json-response-format]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

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
    ;; why does the response have a :response key?
    (let [error (-> response :response :error)]
      (if (= 401 (-> response :status))
        {:dispatch [:handle-unauthorized-request error]}
        {:dispatch [:handle-request-error error]}))))

;; this is used for http requests that don't need failure or success handlers
(reg-event-fx :no-op (fn [] nil))

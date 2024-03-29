(ns gym.frontend.analytics.events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [gym.frontend.config :as cfg]))

(reg-event-db
 :analytics-query-started
 (fn [db [_ query-name]]
   (assoc-in db [:analytics :results query-name :loading] true)))

(reg-event-db
 :analytics-query-succeeded
 (fn [db [_ query-name body]]
   (-> db
       (assoc-in [:analytics :results query-name :loading] false)
       (assoc-in [:analytics :results query-name :data] body)
       (assoc-in [:analytics :results query-name :error] nil))))

(reg-event-db
 :analytics-query-failed
 (fn [db [_ query-name res]]
   (let [error-message (or (:error res) "Unexpected error")]
     (-> db
         (assoc-in [:analytics :results query-name :loading] false)
         (assoc-in [:analytics :results query-name :data] nil)
         (assoc-in [:analytics :results query-name :error] error-message)))))

(reg-event-fx
 :analytics-query
 (fn [_ [_ query]]
   (let [[query-name params] (if (vector? query) query [query {}])]
     {:dispatch-n [[:fetch {:method :get
                            :uri (str cfg/api-url "/api/analytics")
                            :params (merge params {:query query-name})
                            :on-success [:analytics-query-succeeded query-name]
                            :on-failure [:analytics-query-failed query-name]}]
                   [:analytics-query-started query-name]]})))

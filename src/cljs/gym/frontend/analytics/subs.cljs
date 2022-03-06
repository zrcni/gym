(ns gym.frontend.analytics.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :analytics-loading?
 (fn [db _]
   (some :loading (-> db :analytics :results))))

(reg-sub
 :analytics-query
 (fn [db [_ query-name]]
   (-> db :analytics :results query-name)))

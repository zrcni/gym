(ns gym.login.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::auth-status
 (fn [db _] (:auth-status db)))

(ns gym.db
  (:require
   [cljs-time.core :as t]
   [gym.calendar-utils :refer [start-of-week]]))

(def default-db
  {:current-route nil
   :calendar {:start-date (start-of-week (t/now))
              :editing-index nil}})

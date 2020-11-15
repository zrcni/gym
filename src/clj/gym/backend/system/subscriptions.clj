(ns gym.backend.system.subscriptions
  (:require [integrant.core :as ig]
            [gym.backend.workouts.subscriptions.core :as workout-subscriptions]))


(defmethod ig/init-key :system/subscriptions [_ deps]
  (workout-subscriptions/register deps))

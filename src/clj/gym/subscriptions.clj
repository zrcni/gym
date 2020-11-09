(ns gym.subscriptions
  (:require [gym.workouts.subscriptions.core :as workout-subscriptions]))

(defn register []
  (workout-subscriptions/register))

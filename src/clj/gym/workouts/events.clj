(ns gym.workouts.events
  (:require [gym.events.create-domain-event :refer [create-domain-event]]))

(defn workout-created [workout]
  (create-domain-event :workout-created
                       {:workout workout}))

(defn workout-deleted [workout]
  (create-domain-event :workout-deleted
                       {:workout workout}))

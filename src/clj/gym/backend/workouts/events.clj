(ns gym.backend.workouts.events
  (:require [gym.backend.domain-events :refer [create-domain-event]]))

(defn workout-created [workout]
  (create-domain-event :workout-created
                       {:workout workout}))

(defn workout-deleted [workout]
  (create-domain-event :workout-deleted
                       {:workout workout}))

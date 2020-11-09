(ns gym.workouts.subscriptions.core
  (:require [gym.events.domain-events :refer [subscribe-event]]
            [gym.events.core :refer [domain-events]]
            [gym.workouts.subscriptions.after-workout-created :as after-workout-created]
            [gym.workouts.subscriptions.after-workout-deleted :as after-workout-deleted]
            [gym.stats.counters.core :refer [weekly-workout-duration-counter
                                             monthly-workout-duration-counter]]))

(defn register []

  (subscribe-event
   domain-events
   :workout-created
   (after-workout-created/create weekly-workout-duration-counter
                                 monthly-workout-duration-counter))

  (subscribe-event
   domain-events
   :workout-deleted
   (after-workout-deleted/create weekly-workout-duration-counter
                                 monthly-workout-duration-counter)))

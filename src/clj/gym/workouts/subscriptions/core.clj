(ns gym.workouts.subscriptions.core
  (:require [gym.domain-events :refer [subscribe-event]]
            [gym.workouts.subscriptions.after-workout-created :as after-workout-created]
            [gym.workouts.subscriptions.after-workout-deleted :as after-workout-deleted]))

(defn register [{:keys [domain-events
                        workout-duration-counter-weekly
                        workout-duration-counter-monthly]}]

  (subscribe-event
   domain-events
   :workout-created
   (after-workout-created/create workout-duration-counter-weekly
                                 workout-duration-counter-monthly))

  (subscribe-event
   domain-events
   :workout-deleted
   (after-workout-deleted/create workout-duration-counter-weekly
                                 workout-duration-counter-monthly)))

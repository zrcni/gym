(ns gym.stats.counters.workout-duration-counter)

(defprotocol WorkoutDurationCounter
  (get-duration [this key])
  (increment-duration [this key n])
  (decrement-duration [this key n])
  (delete-duration [this key])
  (clear-durations [this]))

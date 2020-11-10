(ns gym.workouts.counters.weekly-counter)

(defprotocol WeeklyCounter
  (incr-count [this key week year count])
  (decr-count [this key week year count])
  (get-count [this key week year]))

(ns gym.workouts.counters.monthly-counter)

(defprotocol MonthlyCounter
  (incr-count [this key month year count])
  (decr-count [this key month year count])
  (get-count [this key month year]))

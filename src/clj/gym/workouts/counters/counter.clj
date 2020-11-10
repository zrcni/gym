(ns gym.workouts.counters.counter)

(defprotocol Counter
  (incr-count [this key count])
  (decr-count [this key count])
  (get-count [this key]))

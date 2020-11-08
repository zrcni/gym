(ns gym.stats.counters.in-memory-workout-duration-counter
  (:require [gym.stats.counters.workout-duration-counter :refer [WorkoutDurationCounter]]
            [gym.util :refer [inc-by dec-by]]))

(defrecord InMemoryWorkoutDurationCounter [data]
  WorkoutDurationCounter

  (increment-duration
    [this key n]
    (swap! data update key (inc-by n)))

  (decrement-duration
    [this key n]
    (swap! data update key (dec-by n)))

  (get-duration
    [this key]
    (or (get @data key) 0))

  (delete-duration
    [this key]
    (swap! data dissoc key))

  (clear-durations
    [this]
    (reset! data {})))



(defn create-in-memory-workout-duration-counter
  ([data] (->InMemoryWorkoutDurationCounter data)))

(ns gym.stats.counters.redis-weekly-counter
  (:require [clojure.string :refer [join]]
            [gym.stats.counters.weekly-counter :refer [WeeklyCounter]]
            [gym.stats.counters.counter :refer [incr-count
                                                decr-count
                                                get-count]]))

(defn key-name [prefix year week user-id]
  (join ":" [prefix (str year) (str week) user-id]))



(defrecord RedisWeeklyCounter [prefix counter]
  WeeklyCounter

  (incr-count [this key week year count]
    (incr-count counter (key-name prefix year week key) count))

  (decr-count [this key week year count]
    (decr-count counter (key-name prefix year week key) count))

  (get-count [this key week year]
    (get-count counter (key-name prefix year week key))))



(defn create-redis-weekly-counter [prefix counter]
  (->RedisWeeklyCounter prefix counter))

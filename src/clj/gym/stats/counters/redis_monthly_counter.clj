(ns gym.stats.counters.redis-monthly-counter
  (:require [clojure.string :refer [join]]
            [gym.stats.counters.monthly-counter :refer [MonthlyCounter]]
            [gym.stats.counters.counter :refer [incr-count
                                                decr-count
                                                get-count]]))

(defn key-name [prefix year month user-id]
  (join ":" [prefix (str year) (str month) user-id]))



(defrecord RedisMonthlyCounter [prefix counter]
  MonthlyCounter

  (incr-count [this key month year count]
    (incr-count counter (key-name prefix year month key) count))

  (decr-count [this key month year count]
    (decr-count counter (key-name prefix year month key) count))

  (get-count [this key month year]
    (get-count counter (key-name prefix year month key))))



(defn create-redis-monthly-counter [prefix counter]
  (->RedisMonthlyCounter prefix counter))

(ns gym.backend.workouts.counters.redis-counter
  (:require [taoensso.carmine :as car :refer (wcar)]
            [gym.backend.workouts.counters.counter :refer [Counter]]))

(defn key-name [key]
  (format "count:%s" key))

(defrecord RedisCounter [redis-conn]
  Counter

  (incr-count [this key count]
              (wcar redis-conn (car/incrby (key-name key) count)))

  (decr-count [this key count]
              (wcar redis-conn (car/decrby (key-name key) count)))

  (get-count [this key]
             (let [count (wcar redis-conn (car/get (key-name key)))]
               (if count (Integer/parseInt count) 0))))



(defn create-redis-counter [redis-conn]
  (->RedisCounter redis-conn))

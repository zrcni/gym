(ns gym.stats.counter
  (:import java.time.LocalDate
           java.time.temporal.TemporalAdjusters
           java.time.DayOfWeek)
  (:require
   [gym.database :refer [get-db]]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]))

(defn inc-by [n]
  (fn [m]
    (if (nil? m) n (+ n m))))

(defn dec-by [n]
  (fn [m]
    (if (nil? m) 0 (- m n))))

;; TODO: figure out how to create an object/class or whatever,
;; because I know this isn't how it should be done because you
;; have to call these methods like this: ((-> counter :inc) "key" 1)
(defn ^:private make-counter [& [data]]
  (let [data (atom (or data {}))]
    {:inc (fn [key & [n]]
            (prn "key:" key)
            (swap! data update key (inc-by (or n 1)))
            (prn "---")
            (prn data)
            (prn "---"))
     :dec (fn [key & [n]]
            (swap! data update key (dec-by (or n 1))))
     :get (fn [key]
            (or (get @data key) 0))
     :del (fn [key]
            (swap! data dissoc key))
     :clear (fn []
              (reset! data {}))}))

(defn format-duration-result [result]
  (assoc result :user_id (.toString (:user_id result))))

(defn get-exercise-durations [start-date end-date]
  (let [results (sql/query (get-db)
                           ["SELECT SUM(duration), user_id FROM workouts WHERE date BETWEEN SYMMETRIC ? AND ? GROUP BY user_id" start-date end-date]
                           {:builder-fn rs/as-unqualified-maps})]
    (map format-duration-result results)))

(defn get-current-week-durations []
  (let [today (LocalDate/now)
        start-of-week (.with today DayOfWeek/MONDAY)
        end-of-week (.with today DayOfWeek/SUNDAY)]
    (get-exercise-durations start-of-week end-of-week)))

(defn get-current-month-durations []
  (let [today (LocalDate/now)
        start-of-month (.with today (TemporalAdjusters/firstDayOfMonth))
        end-of-month (.with today (TemporalAdjusters/lastDayOfMonth))]
    (get-exercise-durations start-of-month end-of-month)))

(defn exercise-durations->counter-data [results]
  (reduce
   (fn [acc result]
     (assoc acc (:user_id result) (/ (:sum result) 1000)))
   {}
   results))

;; Currently the durations are only initialized at server startup.
;; It's not a problem as the production server is currently hosted on Heroku,
;; which shuts down the server when there's no activity.
;; When the server is longer running there should be a job that reinitializes the counters once a day or so.
;; Durations are stored as seconds as opposed to milliseconds like how they're stored in DB
(def current-week-exercise-durations (-> (get-current-week-durations)
                                         exercise-durations->counter-data
                                         make-counter))
(def current-month-exercise-durations (-> (get-current-month-durations)
                                          exercise-durations->counter-data
                                          make-counter))

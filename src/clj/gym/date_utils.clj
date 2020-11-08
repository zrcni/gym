(ns gym.date-utils
  (:import java.time.LocalDate
           java.time.temporal.TemporalAdjusters
           java.time.DayOfWeek
           java.time.Instant))

(defn current-week? [local-date]
  (let [today (LocalDate/now)
        start-of-week (.with today DayOfWeek/MONDAY)
        end-of-week (.with today DayOfWeek/SUNDAY)]
    (and (>= (.compareTo local-date start-of-week) 0)
         (<= (.compareTo local-date end-of-week) 0))))

(defn current-month? [local-date]
  (let [today (LocalDate/now)
        start-of-month (.with today (TemporalAdjusters/firstDayOfMonth))
        end-of-month (.with today (TemporalAdjusters/lastDayOfMonth))]
    (and (>= (.compareTo local-date start-of-month) 0)
         (<= (.compareTo local-date end-of-month) 0))))

(defn instant
  ([ms] (Instant/ofEpochMilli ms))
  ([] (Instant/now)))

(defn instant? [v]
  (instance? Instant v))

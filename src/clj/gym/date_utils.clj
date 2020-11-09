(ns gym.date-utils
  (:import java.time.LocalDate
           java.time.LocalDateTime
           java.time.ZoneOffset
           java.time.temporal.TemporalAdjusters
           java.time.DayOfWeek
           java.time.Instant
           java.time.temporal.WeekFields
           java.util.Locale))

(defn instant->local-date [date]
  (.toLocalDate (LocalDateTime/ofInstant date ZoneOffset/UTC)))

;; LocalDate localDate
;; = LocalDateTime.ofInstant (instantOfNow, ZoneOffset.UTC) .toLocalDate ();

(defn instant
  ([ms] (Instant/ofEpochMilli ms))
  ([] (Instant/now)))

(defn instant? [v]
  (instance? Instant v))

(defn local-date
  ([s] (LocalDate/parse s))
  ([] (LocalDate/now)))

(defn current-week? [date]
  (let [today (local-date)
        start-of-week (.with today DayOfWeek/MONDAY)
        end-of-week (.with today DayOfWeek/SUNDAY)]
    (and (>= (.compareTo date start-of-week) 0)
         (<= (.compareTo date end-of-week) 0))))

(defn current-month? [date]
  (let [today (local-date)
        start-of-month (.with today (TemporalAdjusters/firstDayOfMonth))
        end-of-month (.with today (TemporalAdjusters/lastDayOfMonth))]
    (and (>= (.compareTo date start-of-month) 0)
         (<= (.compareTo date end-of-month) 0))))

(def week-of-year (-> (Locale/getDefault) WeekFields/of .weekOfWeekBasedYear))

(defn get-week-of-year [date]
  (.get date week-of-year))

(defn get-year [date]
  (.getYear date))

(defn get-month
  "1-12"
  [date]
  (.getMonthValue date))

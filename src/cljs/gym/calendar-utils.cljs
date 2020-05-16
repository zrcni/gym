(ns gym.calendar-utils
  (:require
   [reagent.core :as reagent :refer [atom]]
   [goog.date :refer [isLeapYear]]
   [cljs-time.core :as t]))

(def days-in-week 7)
;; number of weeks (rows) to display in the calendar
(def num-weeks 5)

(defn ms->m [ms] (/ ms 1000 60))

(defn m->ms [ms] (* ms 1000 60))

(defn iso->ms [iso-date]
  (-> iso-date
      (js/Date.)
      (.valueOf)))

(defn add-duration [date duration]
  (t/plus date duration))

(defn subtract-duration [date duration]
  (t/minus date duration))

(defn start-of-week [date]
  (let [week-starts-on 1
        day (.getDay date)
        name-this-pls (if (< day week-starts-on) 7 0)
        diff (- (+ name-this-pls day) week-starts-on)]
    (.setDate date (- (.getDate date) diff))
    (.setHours date 0 0 0 0)
    date))

(defn local-date->date-time [local-date]
  (let [[year month day] (as-> local-date s
                           (.split s "-")
                           (map #(js/parseInt %) s))]
    (t/date-time year month day)))

(defn pad-n
  "If a number only has one digit, add 0 to the beginning and return it as a string."
  [n]
  (if (< n 10)
    (str "0" (.toString n))
    (.toString n)))

(defn date-time->local-date [date]
  (let [day (t/day date)
        month (t/month date)
        year (t/year date)]
    (apply str [year "-" (pad-n month) "-" (pad-n day)])))

(defn date-time->dd-mm-yyyy [date & [separator]]
  (let [sep (if separator separator ".")
        day (t/day date)
        month (t/month date)
        year (t/year date)]
    (apply str [day sep month sep year])))

(defn is-first-day-of-month [date]
  (= 1 (t/day date)))

(defn days-in-month [m y]
  (case m
    1 (if (isLeapYear y) 29 28)
    3 30
    5 30
    8 30
    10 30
    :else 31))

(defn human-weekday-short [date]
  (case (t/day-of-week date)
    1 "Mon"
    2 "Tue"
    3 "Wed"
    4 "Thu"
    5 "Fri"
    6 "Sat"
    7 "Sun"))

(defn human-month-short [date]
  (case (t/month date)
    1 "Jan"
    2 "Feb"
    3 "Mar"
    4 "Apr"
    5 "May"
    6 "Jun"
    7 "Jul"
    8 "Aug"
    9 "Sep"
    10 "Oct"
    11 "Nov"
    12 "Dec"))

(defn is-same-day? [a b]
  (= (date-time->local-date a) (date-time->local-date b)))

(defn is-future? [a b]
  (t/after? a b))

(defn map-workouts-by-day [workouts]
  (reduce
   (fn [by-day workout]
     (let [date (:date workout)]
       (as-> by-day d
        (if-not (get d date) (assoc d date []) d)
        (update d date conj workout))))
   {}
   workouts))

(defn calculate-weeks [start-date & [workouts]]
  (let [cursor (atom -1)
        workouts-by-day (map-workouts-by-day workouts)]
    (reduce-kv
     (fn [weeks week-index]
       (let [local-date (-> start-date
                            (start-of-week)
                            (t/plus (t/days week-index))
                            (date-time->local-date))
             workouts (get workouts-by-day local-date)
             day-data {:local-date local-date
                       :workouts (when workouts
                                   (->> workouts
                                        (sort #(compare (iso->ms (:created_at %1))
                                                        (iso->ms (:created_at %2))))))}]
         (if (< 0 (mod week-index days-in-week))
           (assoc weeks @cursor (conj (get weeks @cursor) day-data))
           (do
             (swap! cursor inc)
             (conj weeks [day-data])))))
     []
     (vec (replicate (* num-weeks days-in-week) nil)))))

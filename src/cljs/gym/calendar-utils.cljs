(ns gym.calendar-utils
  (:require
   [goog.date :refer [isLeapYear]]
   [cljs-time.core :as t]))

(defn add-time [date time]
  (t/plus date time))

(defn subtract-time [date time]
  (t/minus date time))

(defn start-of-week [date]
  (let [week-starts-on 1
        day (.getDay date)
        name-this-pls (if (< day week-starts-on) 7 0)
        diff (- (+ name-this-pls day) week-starts-on)]
    (.setDate date (- (.getDate date) diff))
    (.setHours date 0 0 0 0)
    date))

(defn day-month-year [date]
  (let [day (t/day date)
        month (t/month date)
        year (t/year date)]
    (apply str [day "." month "." year])))

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
  (= (day-month-year a) (day-month-year b)))

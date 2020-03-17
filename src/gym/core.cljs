(ns gym.core
    (:require
     [reagent.core :as reagent :refer [atom]]
      [goog.date :refer [isLeapYear]]
      [cljs-time.core :as t]))

(enable-console-print!)

(defn start-of-week [date]
  (let [week-starts-on 1
        day (.getDay date)
        name-this-pls (if (< day week-starts-on) 7 0)
        diff (- (+ name-this-pls day) week-starts-on)]
    (.setDate date (- (.getDate date) diff))
    (.setHours date 0 0 0 0)
    date))

(defonce app-state (atom {:start-date (start-of-week (t/now))}))

(defn dd-mm-yyyy [date]
  (let [day (t/day date)
        month (t/month date)
        year (t/year date)]
    (apply str [day "-" month "-" year])))

(defn calculate-weeks [start-date num-weeks]
  (let [cursor (atom -1)
        days-in-week 7]
    (reduce-kv
     (fn [weeks i]
       (let [data {:date (t/plus start-date (t/days i))}]
         (if (< 0 (mod i 7))
           (assoc weeks @cursor (conj (get weeks @cursor) data))
           (do
             (swap! cursor inc)
             (conj weeks [data])))))
     []
     (vec (replicate (* num-weeks days-in-week) nil)))))

(defn days-in-month [m y]
  (case m
    1 (if (isLeapYear y) 29 28)
    3 30
    5 30
    8 30
    10 30
    :else 31))

(defn weekdays []
  [:div.Weekdays
   [:div "Monday"]
   [:div "Tuesday"]
   [:div "Wednesday"]
   [:div "Thursday"]
   [:div "Friday"]
   [:div "Saturday"]
   [:div "Sunday"]])

(defn calendar-nav [{:keys [show-later on-earlier-click on-later-click]}]
  [:div.Calendar_nav
   [:button.Calendar_earlier.icon_button {:on-click on-earlier-click}
    [:span "Earlier"]]
   (when show-later
     [:button.Calendar_later.icon_button {:on-click on-later-click}
      [:span "Later"]])])

(defn is-first-day-of-month [date]
  (= 1 (t/day date)))

(defn is-first-displayed-day [day-index week-index]
  (= 0 (+ day-index week-index)))

(defn should-show-month [day-index week-index date]
  (when (or
         (is-first-displayed-day day-index week-index)
         (is-first-day-of-month date))
    true))

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
  (= (dd-mm-yyyy a) (dd-mm-yyyy b)))

; renders a calendar which is displayed in the following format (days-in-week * n)
;  m t w t f s s
;; - - - - - - -
;; - - - - - - -
;; - - - - - - -
;; - - - - - - -
;; - - - - - - -

(defn calendar []
  (let [num-weeks 5
        show-earlier (fn []  (swap! app-state assoc :start-date (t/minus (:start-date @app-state) (t/days (* num-weeks 7)))))
        show-later (fn [] (swap! app-state assoc :start-date (t/plus (:start-date @app-state) (t/days (* num-weeks 7)))))]
    (fn []
      (let [start-date (:start-date @app-state)
            weeks (calculate-weeks start-date num-weeks)]
        [:<>
         [:div.Calendar
          [weekdays]
          [:div.Calendar_animation_overflow
           (map-indexed
            (fn [week-index week]
              [:div.Calendar_week {:key week-index}
               (map-indexed
                (fn [day-index day]
                  [:div.Day.Day_is_future.Day_no_minutes {:key (:date day)}
                   [:div.Day_date
                    (when (should-show-month day-index week-index (:date day))
                      [:div.Day_month (human-month-short (:date day))])
                    [:div.Day_number (t/day (:date day))]]
                                                          ; TODO: display data about the date's activities
                   [:div.Day_minutes
                    [:button.Calendar_add_post_button "+"]]
                   (when (is-same-day? (:date day) (t/now))
                     [:div.Day_today "Today"])])
                week)])
            weeks)]]
         [calendar-nav {:show-later (not (is-same-day? start-date (t/now)))
                        :on-earlier-click show-earlier
                        :on-later-click show-later}]]))))

(defn app []
  [:div
   [calendar]])

(reagent/render-component [app]
  (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

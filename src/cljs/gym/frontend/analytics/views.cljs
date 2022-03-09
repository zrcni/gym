(ns gym.frontend.analytics.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [recharts]
            [gym.frontend.components.loaders :as loaders]
            [clojure.contrib.humanize :as humanize]
            [clojure.string :refer [capitalize]]))

(def bar-colors
  ["#176BA0"
   "#19AADE"
   "#FDA58F"
   "#C02323"
   "#EF7E32"
   "#FF5733"
   "#3F5DC0"
   "#A1414B"
   "#52A343"
   "#41A15D"
   "#ACB232"
   "#43A376"
   "#7027B2"
   "#DAF7A6"
   "#FFC300"
   "#43A3A2"])

(def bar-color-default
  (first bar-colors))

(defn human-duration [duration]
  (if (= 0 duration)
    "None"
    (as-> duration d
      (humanize/duration d {:number-format str})
      (capitalize d))))



(defn chart-view [{:keys [title chart data error loading]}]
  [:div {:style {:height 400}}
   [:h4 title]
   (cond
     error
     [:div (str "Query failed: " error " :(")]

     loading
     [:div.chart-loading-container
      [loaders/circle {:size 160}]]

     data
     [:> recharts/ResponsiveContainer {:width "100%" :height 400}
      (chart data)])])



(defn workout-duration-by-tags-chart [{:keys [result]}]
  [chart-view
   (merge
    result
    {:title "Duration by tag, all time"
     :chart
     (fn [data]
       [:> recharts/BarChart {:data data}
        [:> recharts/XAxis {:dataKey "tag"}]
        [:> recharts/Tooltip {:formatter #(array (human-duration %) nil)}]
        [:> recharts/Bar {:dataKey "duration"
                          :fill bar-color-default}]])})])

(def week-of-day-num->human
  {1 "Monday"
   2 "Tuesday"
   3 "Wednesday"
   4 "Thursday"
   5 "Friday"
   6 "Saturday"
   7 "Sunday"})

(defn workouts-by-day-of-week-chart [{:keys [result]}]
  [chart-view
   (merge
    result
    {:title "Exercises by day of week, all time"
     :chart
     (fn [data]
       [:> recharts/BarChart {:data (:entries data)}
        [:> recharts/XAxis {:dataKey "date"
                            :tickFormatter #(week-of-day-num->human %)}]
        [:> recharts/YAxis]
        [:> recharts/Tooltip {:separator ": "
                              :labelFormatter #(week-of-day-num->human %)}]
        [:> recharts/Legend]
        (map-indexed
         (fn [i bar-name]
           [:> recharts/Bar {:key bar-name
                             :dataKey bar-name
                             :stackId "a"
                             :fill (nth bar-colors i bar-color-default)}])
         (:all-tags data))])})])



(def month-of-year-num->human
  {1 "January"
   2 "February"
   3 "March"
   4 "April"
   5 "May"
   6 "June"
   7 "July"
   8 "August"
   9 "September"
   10 "October"
   11 "November"
   12 "December"})

(defn workouts-by-month-of-year-chart [{:keys [result]}]
  [chart-view
   (merge
    result
    {:title "Exercises by month of year, all time"
     :chart
     (fn [data]
       [:> recharts/BarChart {:data (:entries data)}
        [:> recharts/XAxis {:dataKey "date"
                            :tickFormatter #(month-of-year-num->human %)}]
        [:> recharts/YAxis]
        [:> recharts/Tooltip {:separator ": "
                              :labelFormatter #(month-of-year-num->human %)}]
        [:> recharts/Legend]
        (map-indexed
         (fn [i bar-name]
           [:> recharts/Bar {:key bar-name
                             :dataKey bar-name
                             :stackId "a"
                             :fill (nth bar-colors i bar-color-default)}])
         (:all-tags data))])})])

(defn analytics-view [{:keys [loading?]}]
  (let [workout-duration-by-tag @(subscribe [:analytics-query :workout-duration-by-tag])
        workouts-by-day-of-week @(subscribe [:analytics-query :workouts-by-day-of-week])
        workouts-by-month-of-year @(subscribe [:analytics-query :workouts-by-month-of-year])]
    [:div
     (if loading?
       [loaders/circle {:size 160}]
       [:div.analytics-container
        [workout-duration-by-tags-chart {:result workout-duration-by-tag}]
        [workouts-by-day-of-week-chart {:result workouts-by-day-of-week}]
        [workouts-by-month-of-year-chart {:result workouts-by-month-of-year}]])]))



(defn main []
  (let [excluded-tags @(subscribe [:excluded-tags])
        params (when-not (empty? excluded-tags) {:exclude excluded-tags})
        init-loading? @(subscribe [:loading :fetch-initial-user-prefs])]

    (when-not init-loading?
      (dispatch [:analytics-query [:workout-duration-by-tag params]])
      (dispatch [:analytics-query [:workouts-by-day-of-week params]])
      (dispatch [:analytics-query [:workouts-by-month-of-year params]]))

    [analytics-view]))

(ns gym.frontend.analytics.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [recharts]
            [gym.frontend.components.loaders :as loaders]
            [clojure.contrib.humanize :as humanize]
            [clojure.string :refer [capitalize]]))

(defn human-duration [duration]
  (if (= 0 duration)
    "None"
    (as-> duration d
      (humanize/duration d {:number-format str})
      (capitalize d))))

(defn workout-duration-by-tags-chart [{:keys [data error]}]
  (let [theme @(subscribe [:theme])]
    [:div
     [:h4 "Duration by tag, all time"]
     (when error
       [:div (str "Query failed: " error " :(")])
     (when data
       [:> recharts/ResponsiveContainer {:width "100%" :height 400}
        [:> recharts/BarChart {:data data}
         [:> recharts/XAxis {:dataKey "tag"}]
         [:> recharts/Tooltip {:formatter #(array (human-duration %) nil)}]
         [:> recharts/Bar {:dataKey "duration"
                           :fill (:theme-color theme)}]]])]))

(def week-of-day-num->human
  {1 "Monday"
   2 "Tuesday"
   3 "Wednesday"
   4 "Thursday"
   5 "Friday"
   6 "Saturday"
   7 "Sunday"})

;; TODO: different colored bars
(defn workouts-by-day-of-week-chart [{:keys [data error]}]
  (let [theme @(subscribe [:theme])
        bars (into #{} (map :tag data))
        data (->> data
                  (group-by :date)
                  (mapv (fn [[date maps]]
                          (reduce (fn [acc {:keys [tag count]}]
                                    (assoc acc tag count))
                                  {:date date}
                                  maps))))]
    [:div
     [:h4 "Exercises by day of week, all time"]
     (when error
       [:div (str "Query failed: " error " :(")])
     (when data
       [:> recharts/ResponsiveContainer {:width "100%" :height 400}
        [:> recharts/BarChart {:data data}
         [:> recharts/XAxis {:dataKey "date"
                             :tickFormatter #(week-of-day-num->human %)}]
         [:> recharts/YAxis]
         [:> recharts/Tooltip {:separator ": "
                               :labelFormatter #(week-of-day-num->human %)}]
         [:> recharts/Legend]
         (map
          (fn [bar-name]
            [:> recharts/Bar {:key bar-name
                              :dataKey bar-name
                              :stackId "a"
                              :fill (:theme-color theme)}])
          bars)]])]))



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

;; TODO: different colored bars
(defn workouts-by-month-of-year-chart [{:keys [data error]}]
  (let [theme @(subscribe [:theme])
        bars (into #{} (map :tag data))
        data (->> data
                  (group-by :date)
                  (mapv (fn [[date maps]]
                          (reduce (fn [acc {:keys [tag count]}]
                                    (assoc acc tag count))
                                  {:date date}
                                  maps))))]
    [:div
     [:h4 "Exercises by day of week, all time"]
     (when error
       [:div (str "Query failed: " error " :(")])
     (when data
       [:> recharts/ResponsiveContainer {:width "100%" :height 400}
        [:> recharts/BarChart {:data data}
         [:> recharts/XAxis {:dataKey "date"
                             :tickFormatter #(month-of-year-num->human %)}]
         [:> recharts/YAxis]
         [:> recharts/Tooltip {:separator ": "
                               :labelFormatter #(month-of-year-num->human %)}]
         [:> recharts/Legend]
         (map
          (fn [bar-name]
            [:> recharts/Bar {:key bar-name
                              :dataKey bar-name
                              :stackId "a"
                              :fill (:theme-color theme)}])
          bars)]])]))



(defn main []
  (dispatch [:analytics-query :duration-by-tag])
  (dispatch [:analytics-query :workouts-by-day-of-week])
  (dispatch [:analytics-query :workouts-by-month-of-year])
  
  (fn []
    (let [loading? @(subscribe [:analytics-loading?])
          duration-by-tag @(subscribe [:analytics-query :duration-by-tag])
          workouts-by-day-of-week @(subscribe [:analytics-query :workouts-by-day-of-week])
          workouts-by-month-of-year @(subscribe [:analytics-query :workouts-by-month-of-year])]
      [:div
       (if loading?
         [loaders/circle]
         [:div.analytics-container
          [workout-duration-by-tags-chart duration-by-tag]
          [workouts-by-day-of-week-chart workouts-by-day-of-week]
          [workouts-by-month-of-year-chart workouts-by-month-of-year]])])))

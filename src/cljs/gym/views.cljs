(ns gym.views
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as reagent :refer [atom]]
   [goog.string.format]
   [gym.events]
   [gym.subs]
   [cljs-time.core :as t]
   ["react-modal" :as Modal]
   [gym.calendar-utils :refer [human-weekday-short
                               day-month-year
                               start-of-week
                               is-same-day?
                               is-first-day-of-month
                               human-month-short]]))

((.-setAppElement Modal) "#app")

(def ^{:private true} days-in-week 7)

(defn modal []
  (fn [{:keys [disable-auto-close is-open on-close title]} & children]
    [:> Modal {:is-open (if (nil? is-open) true is-open)
               :style {:content {:margin 0 :padding 0}}
               :on-request-close #(when-not (nil? on-close) (on-close))
               :content-label title
               :should-close-on-overlay-click (not disable-auto-close)}
     [:div {:class "container-fluid"}
      (when title
        [:div {:class "row"
               :style {:padding 12
                       :background-color "#581845"
                       :color "white"
                       :display "flex"
                       :justify-content "space-between"}}
         [:span title]
         (when-not disable-auto-close
           [:button {:style {:background "none" :border "none" :color "white"} :on-click #(when-not (nil? on-close) (on-close))}
            [:i.fas.fa-times]])])
      [:div {:style {:margin 8}}
       children]]]))

;; -------------------------
;; Routes
(defn layout []
  (fn [_ & children]
    [:<>
     [:header {:id "header" :class "navbar navbar-expand navbar-dark flex-column flex-md-row bd-navbar"}]
     [:main {:id "content"}
      children]]))

;; -------------------------
;; Page components

; Basically copy-pasted the calendar functionality (and look) from this repo:
; https://github.com/ReactTraining/hooks-workshop

(defn calculate-weeks [start-date num-weeks]
  (let [cursor (atom -1)]
    (reduce-kv
     (fn [weeks i]
       (let [data {:date (t/plus start-date (t/days i))}]
         (if (< 0 (mod i days-in-week))
           (assoc weeks @cursor (conj (get weeks @cursor) data))
           (do
             (swap! cursor inc)
             (conj weeks [data])))))
     []
     (vec (replicate (* num-weeks days-in-week) nil)))))

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
    [:i.fas.fa-chevron-up]
    [:span "Earlier"]]
   (when show-later
     [:button.Calendar_later.icon_button {:on-click on-later-click}
      [:i.fas.fa-chevron-down]
      [:span "Later"]])])

(defn is-first-displayed-day [day-index week-index]
  (= 0 (+ day-index week-index)))

(defn should-show-month? [day-index week-index date]
  (if (or
       (is-first-displayed-day day-index week-index)
       (is-first-day-of-month date))
    true
    false))

(defn day-title [date]
  (as-> (str (human-weekday-short date) " " (day-month-year date)) title
    (if (is-same-day? date (t/now))
      (str title " - today")
    title)))

;; (defn day-title [date]
;;   (str (day-month-year date) (when (is-same-day? date (t/now)) " - Today")))

; renders a calendar which is displayed in the following format (days-in-week * n)
;  m t w t f s s
;; - - - - - - -
;; - - - - - - -
;; - - - - - - -
;; - - - - - - -
;; - - - - - - -

(defn calendar []
  (let [start-date @(subscribe [:calendar-start-date])
        editing-index @(subscribe [:calendar-editing-index])
        num-weeks 5
        weeks (calculate-weeks start-date num-weeks)
        edit-day (fn [day-index] (dispatch [:calendar-edit-day day-index]))
        stop-editing #(dispatch [:calendar-stop-editing])
        show-earlier #(dispatch [:calendar-show-earlier (t/days (* num-weeks days-in-week))])
        show-later #(dispatch [:calendar-show-later (t/days (* num-weeks days-in-week))])]
    [:<>
     [:div.Calendar_year (t/year start-date)]
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
                (when (should-show-month? day-index week-index (:date day))
                  [:div.Day_month (human-month-short (:date day))])
                [:div.Day_number (t/day (:date day))]]
                                                          ; TODO: display data about the date's activities
               [:div.Day_minutes
                [:button.Calendar_add_post_button {:on-click #(edit-day (+ (* week-index days-in-week) day-index))}
                 [:i.fas.fa-plus]]]
               (when (is-same-day? (:date day) (t/now))
                 [:div.Day_today "Today"])
               (when (= editing-index (+ (* week-index days-in-week) day-index))
                 [modal {:title (day-title (:date day)) :on-close stop-editing}
                  [:div]])])
            week)])
        weeks)]]
     [calendar-nav {:show-later (not (is-same-day? start-date (start-of-week (t/now))))
                    :on-earlier-click show-earlier
                    :on-later-click show-later}]]))

(defn home-page []
  [calendar])

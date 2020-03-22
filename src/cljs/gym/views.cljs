(ns gym.views
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as reagent]
   [goog.string.format]
   [gym.events]
   [gym.subs]
   [cljs-time.core :as t]
   ["react-modal" :as Modal]
   [gym.calendar-utils :refer [ms->m
                               num-weeks
                               days-in-week
                               human-weekday-short
                               day-month-year
                               start-of-week
                               is-same-day?
                               is-first-day-of-month
                               human-month-short]]))

((.-setAppElement Modal) "#app")

(defn modal []
  (fn [{:keys [disable-auto-close is-open on-close title]} & children]
    [:> Modal {:is-open (if (nil? is-open) true is-open)
               :on-request-close #(when-not (nil? on-close) (on-close))
               :content-label title
               :should-close-on-overlay-click (not disable-auto-close)
               :overlay-class-name "modal-overlay-custom"
               :class "modal-content-custom"
               }
     [:div.container-fluid
      (when title
        [:div.row.modal-title
         [:span title]
         (when-not disable-auto-close
           [:button.modal-close-button {:on-click #(when-not (nil? on-close) (on-close))}
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
    [:i.fas.fa-chevron-up.purple-icon]
    [:span "Earlier"]]
   (when show-later
     [:button.Calendar_later.icon_button {:on-click on-later-click}
      [:i.fas.fa-chevron-down.purple-icon]
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

(defn new-workout []
  (fn [{:keys [date]}]
    [:form.NewPost_form
     [:div.NewPost_]
     [:textarea.NewPost_input {:placeholder "How did you workout?"}]
     [:div.NewPost_buttons
      [:button.icon_button.cta "Submit"]]]))

(defn created-workouts []
  (let [adding (reagent/atom false)]
    (fn [{:keys [date workouts]}]
      [:div.Posts_content
       [:div.Posts_posts
        (map
         (fn [workout] [:div.Post
                        [:div.Post_title
                         [:div.Post_minutes
                          [:span (str (ms->m (:duration workout)) " minutes")]]
                         [:button.Post_delete_button.icon_button
                          [:i.fas.fa-trash-alt
                           [:span " Delete"]]]]
                        [:div.Post_message (:description workout)]])
         workouts)]
       (if @adding
         [:div.Posts_adding
          [new-workout]]

         [:div.Posts_add
          [:button.Posts_add_button.icon_button {:on-click #(reset! adding true)}
           [:i.fas.fa-plus-circle
            [:span " Add another"]]]])])))

(defn calculate-total-workout-minutes [workouts]
  (reduce
   (fn [minutes workout]
     (+ minutes (:duration workout)))
   0
   workouts))

; renders a calendar which is displayed in the following format (days-in-week * n)
;  m t w t f s s
;; - - - - - - -
;; - - - - - - -
;; - - - - - - -
;; - - - - - - -
;; - - - - - - -

(defn calendar []
  (dispatch [:fetch-all-workouts])
  (fn []
    (let [start-date @(subscribe [:calendar-start-date])
          editing-index @(subscribe [:calendar-editing-index])
          weeks @(subscribe [:calendar-weeks])
          edit-day #(dispatch [:calendar-edit-day %])
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
                   (if (:workouts day)
                     (let [total-minutes (ms->m (calculate-total-workout-minutes (:workouts day)))]
                       [:div total-minutes])
                     [:i.fas.fa-plus.purple-icon])]]

                 (when (is-same-day? (:date day) (t/now))
                   [:div.Day_today "Today"])

                 (when (= editing-index (+ (* week-index days-in-week) day-index))
                   [modal {:title (day-title (:date day)) :on-close stop-editing}
                    (if (:workouts day)
                      [created-workouts {:date (:date day)
                                         :workouts (:workouts day)}]
                      [new-workout {:date (:date day)}])])])
              week)])
          weeks)]]
       [calendar-nav {:show-later (not (is-same-day? start-date (start-of-week (t/now))))
                      :on-earlier-click show-earlier
                      :on-later-click show-later}]])))

(defn home-page []
  [calendar])

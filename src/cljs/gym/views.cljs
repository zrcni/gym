(ns gym.views
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as reagent]
   [clojure.string :refer [trim blank? join]]
   [goog.string.format]
   [gym.events]
   [gym.subs]
   [gym.util :refer [includes?]]
   [react-modal]
   [cljs-time.core :as t]
   [gym.calendar-utils :refer [ms->m
                               m->ms
                               num-weeks
                               days-in-week
                               local-date->date-time
                               date-time->dd-mm-yyyy
                               human-weekday-short
                               start-of-week
                               is-same-day?
                               is-first-day-of-month
                               human-month-short]]))

(.setAppElement js/ReactModal "#app")

(defn modal []
  (fn [{:keys [disable-auto-close is-open on-close title]} & children]
    [:> js/ReactModal {:is-open (if (nil? is-open) true is-open)
          :on-request-close #(when-not (nil? on-close) (on-close))
          :content-label title
          :should-close-on-overlay-click (not disable-auto-close)
          :overlay-class-name "modal-overlay-custom"
          :class "modal-content-custom"}
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
    (let [user @(subscribe [:user])]
      [:<>
       [:header {:id "header" :class "navbar navbar-expand navbar-dark flex-column flex-md-row bd-navbar"}
        (when user [:button.logout-button {:on-click #(dispatch [:logout])} "Logout"])]
       [:main {:id "content"}
        children]])))

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

(defn should-show-month? [day-index week-index local-date]
  (if (or
       (is-first-displayed-day day-index week-index)
       (is-first-day-of-month (local-date->date-time local-date)))
    true
    false))

(defn day-title [local-date]
  (let [date (local-date->date-time local-date)]
    (as-> (str (human-weekday-short date) " " (date-time->dd-mm-yyyy date)) title
      (if (is-same-day? date (t/now))
        (str title " - today")
        title))))

(defn to-number
  "Converts a string into a number or just returns the number. Returns 0 if the parsed string converts into NaN"
  [v]
  (if (and (number? v) (not= v js/NaN))
    v
    (let [n (js/parseInt v)]
      (if (= n js/NaN) 0 n))))

(defn tag-chip [{:keys [value on-delete]}]
  [:span.tag
   value
   (when on-delete
     [:span.tag-delete {:role "img"
                        :aria-label "delete"
                        :on-click #(on-delete value)} "X"])])

(defn add-tag [{:keys [on-add]}]
  (let [tag (reagent/atom "")
        add #(when-not (empty? (trim @tag))
               (on-add @tag)
               (reset! tag ""))
        on-key-down #(when (= 13 (.-keyCode %)) (add))]
    (fn []
      [:div
       [:input.tag-add-input {:type "text"
                              :value @tag
                              :on-change #(reset! tag (-> % .-target .-value))
                              :on-key-down on-key-down}]
       [:button.tag-add-button {:type "button" :on-click add} "Add"]])))

(defn exercise-tags [{:keys [tags on-add on-delete]}]
  [:div.NewPost_tags
   [add-tag {:on-add on-add}]
   [:div.tags
    (map (fn [tag] ^{:key tag} [tag-chip {:value tag
                                          :on-delete on-delete}]) tags)]])

(defn new-workout [{:keys [local-date]}]
  ;; TODO: persist state, so closing the modal doesn't wipe the data
  (let [state (reagent/atom {:minutes 30
                             :description ""
                             :tags []})
        min-minutes 0
        update-minutes #(swap! state assoc :minutes %)
        update-description #(swap! state assoc :description %)
        handle-description-change #(update-description (-> % .-target .-value))
        handle-minutes-change #(update-minutes (-> % .-target .-value))
        inc-minutes #(let [n (to-number (:minutes @state))]
                       (update-minutes (inc n)))
        dec-minutes #(let [n (to-number (:minutes @state))]
                       (when (> n min-minutes)
                         (update-minutes (dec n))))
        add-tag #(when-not (or (includes? (:tags @state) %) (blank? %))
                   (swap! state assoc :tags (conj (:tags @state) %)))
                 delete-tag #(swap! state assoc :tags (filter (fn [tag] (not= tag %)) (:tags @state)))
        create-exercise #(dispatch [:create-workout-request {:date local-date
                                                             :description (:description @state)
                                                             :duration (-> (:minutes @state)
                                                                           (to-number)
                                                                           (m->ms))
                                                             :tags (:tags @state)}])]
    (fn []
      [:div.NewPost_form
       [:textarea.NewPost_input {:placeholder "How did you exercise?"
                                 :value (:description @state)
                                 :on-change handle-description-change}]
       [exercise-tags {:tags (:tags @state)
                       :on-add add-tag
                       :on-delete delete-tag}]
       [:div.NewPost_buttons
        [:div.Minutes
         [:div
          [:button.Minutes_button.icon_button {:type "button"
                                               :on-click #(dec-minutes)}
           [:i.fas.fa-minus]]]
         [:input.Minutes_input {:id "minutes"
                                ;; Even though this input controls a number I don't want to use a number input type,
                                ;; because that would add the DOM's own increment and decrement buttons which I don't want.
                                :type "text"
                                :value (:minutes @state)
                                :on-change handle-minutes-change}]
         [:button.Minutes_button.icon_button {:type "button"
                                              :on-click #(inc-minutes)}
          [:i.fas.fa-plus]]
         [:label.Minutes_label {:for "minutes"} "minutes"]]
        [:button.icon_button.cta {:type "button" :on-click create-exercise} "Submit"]]])))

(defn created-workouts []
  (let [adding (reagent/atom false)
        delete-workout #(dispatch [:delete-workout %])]

    (fn [{:keys [local-date workouts]}]
      [:div.Posts_content
       [:div.Posts_posts
        (map
         (fn [workout]
           ^{:key (:workout_id workout)}
           [:div.Post
            [:div.Post_title
             [:div.Post_minutes
              [:span (str (ms->m (:duration workout)) " minutes")]]
             [:button.Post_delete_button.icon_button {:on-click #(delete-workout (:workout_id workout))}
              [:i.fas.fa-trash-alt
               [:span " Delete"]]]]
            [:div.Post_message (:description workout)]
            [:div.Post_tags (str "tags: " (join ", " (:tags workout)))]])
         workouts)]
       (if @adding
         [:div.Posts_adding
          [new-workout {:local-date local-date}]]

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
            ^{:key week-index} [:div.Calendar_week
             (map-indexed
              (fn [day-index day]
                (let [parsed-date (local-date->date-time (:local-date day))]
                  ^{:key (:local-date day)} [:div.Day.Day_is_future.Day_no_minutes
                                             [:div.Day_date
                                              (when (should-show-month? day-index week-index (:local-date day))
                                                [:div.Day_month (human-month-short parsed-date)])
                                              [:div.Day_number (t/day parsed-date)]]
                                                          ; TODO: display data about the date's activities
                                             [:div.Day_minutes
                                              [:button.Calendar_add_post_button {:on-click #(edit-day (+ (* week-index days-in-week) day-index))}
                                               (if (:workouts day)
                                                 (let [total-minutes (ms->m (calculate-total-workout-minutes (:workouts day)))]
                                                   [:div total-minutes])
                                                 [:i.fas.fa-plus.purple-icon])]]

                                             (when (is-same-day? parsed-date (t/now))
                                               [:div.Day_today "Now"])

                                             (when (= editing-index (+ (* week-index days-in-week) day-index))
                                               [modal {:title (day-title (:local-date day)) :on-close stop-editing}
                                                (if (:workouts day)
                                                  ^{:key "created-workouts"} [created-workouts {:local-date (:local-date day)
                                                                                                :workouts (:workouts day)}]
                                                  ^{:key "new-workout"} [new-workout {:local-date (:local-date day)}])])]))
              week)])
          weeks)]]
       [calendar-nav {:show-later (not (is-same-day? start-date (start-of-week (t/now))))
                      :on-earlier-click show-earlier
                      :on-later-click show-later}]])))

(defn home-page []
  [calendar])

;; TODO: style
(defn login-page []
  (fn []
    [:button#login {:on-click #(dispatch [:login-auth0])} "Click to login"]))

(defn login-callback-page []
  (dispatch [:handle-login-auth0-callback])
  (fn [] [:div.circle-loader]))

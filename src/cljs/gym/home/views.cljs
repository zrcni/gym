(ns gym.home.views
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as reagent]
   [clojure.string :refer [capitalize trim blank? join]]
   [goog.string.format]
   [gym.util :refer [includes?]]
   [react-modal]
   [react-contenteditable]
   [emojiMart]
   [smileParser]
   [clojure.contrib.humanize :as humanize]
   [cljs-time.core :as t]
   [cljss.core :refer-macros [defstyles]]
   [cljss.reagent :refer-macros [defstyled]]
   [gym.components.icons :as icons]
   [gym.styles :as styles :refer [classes]]
   [gym.components.emoji-picker :refer [emoji-picker parse-emojis modal]]
   [gym.components.modal :refer [modal]]
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
                               human-month-short
                               is-future?]]))

(defstyles weekdays-style []
  {:display "flex"
   :flex 1
   :margin 2
   "> *" {:text-transform "uppercase"
          :flex 1
          :text-align "center"
          :font-weight "bold"
          :color styles/gray
          :font-size "85%"}})

(defn weekdays []
  [:div {:class (weekdays-style)}
   [:div "Mon"]
   [:div "Tue"]
   [:div "Wed"]
   [:div "Thu"]
   [:div "Fri"]
   [:div "Sat"]
   [:div "Sun"]])

(defstyles calendar-nav-style []
  {:display "flex"
   :margin "0.5rem"
   :font-size "85%"})

(defstyles calendar-earlier-later []
  {:&:hover {:background-color "white"}})

(defn calendar-nav [{:keys [show-later on-earlier-click on-later-click]}]
  [:div {:class (calendar-nav-style)}
   [:button {:on-click on-earlier-click :class (classes (calendar-earlier-later) (styles/icon-button))}
    [icons/chevron-up {:class (styles/base-icon)}]
    [:span "Earlier"]]
   (when show-later
     [:button {:on-click on-later-click :class (classes (calendar-earlier-later) (styles/icon-button))}
      [icons/chevron-down {:class (styles/icon-button)}]
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

(defstyles tag-delete-style []
  {:display "inline-block"
   :font-size "10px"
   :margin-left "3px"
   :color "rgba(0, 0, 0, 0.45)"
   :font-weight 700
   :cursor "pointer"
   :transition "all 0.3s cubic-bezier(0.78, 0.14, 0.15, 0.86)"})

(defstyles tag-style []
  {:box-sizing "border-box"
   :margin 0
  ;;  :padding 0
   :color "rgba(0, 0, 0, 0.65)"
  ;;  :font-size 14
  ;;  :line-height 1.5715
   :list-style "none"
   :font-feature-settings "tabular-nums"
   :display "inline-block"
   :height "auto"
   :padding "0px 7px"
   :font-size "12px"
   :line-height "20px"
   :white-space "nowrap"
   :background "#fafafa"
   :border "1px solid #d9d9d9"
   :border-radius "2px"
   :cursor "default"
   :opacity 1
   :transition "all 0.3s cubic-bezier(0.78, 0.14, 0.15, 0.86)"
   :margin-left "4px"
   :vertical-align "middle"})

(defn tag-chip [{:keys [value on-delete]}]
  [:span {:class (tag-style)}
   value
   (when on-delete
     [:span {:role "img"
             :aria-label "delete"
             :class (tag-delete-style)
             :on-click #(on-delete value)} "X"])])

(defstyles tag-add-input-style []
  {:border "1px solid #ebebeb"
   :margin-top "6px"
   :margin-bottom "6px"
   :width "100px"
   :height "27px"
   :padding "4px"})

(defstyles tag-add-button-style []
  {:border "1px solid #ebebeb"
   :font-size "85%"
   :margin-bottom "6px"
   :margin-top "6px"
   :padding-top "1px"
   :height "27px"
   :padding-left "4px"
   :padding-right "4px"})

(defn add-tag [{:keys [on-add]}]
  (let [tag (reagent/atom "")
        add #(when-not (empty? (trim @tag))
               (on-add @tag)
               (reset! tag ""))
        on-key-down #(when (= 13 (.-keyCode %)) (add))]
    (fn []
      [:div
       [:input {:class (tag-add-input-style)
                :type "text"
                :value @tag
                :placeholder "tags"
                :on-change #(reset! tag (-> % .-target .-value))
                :on-key-down on-key-down}]
       [:button {:class (tag-add-button-style)
                 :type "button"
                 :on-click add} "Add"]])))

(defstyles new-exercise-tags-style []
  {:display "inline-block"})

(defstyles new-exercise-tags-wrapper-style []
  {:display "inline-block"
   :vertical-align "middle"})

(defn exercise-tags [{:keys [tags on-add on-delete]}]
  [:div {:class (new-exercise-tags-style)}
   [add-tag {:on-add on-add}]
   [:div {:class (new-exercise-tags-wrapper-style)}
    (map (fn [tag] ^{:key tag}
           [tag-chip {:value tag
                      :on-delete on-delete}]) tags)]])

(defstyles new-exercise-form-style []
  {:position "relative"
   :margin "0px"
   :flex 1
   "> *:first-child" {:margin-top "1.25rem"}})

(defstyles new-exercise-input-style []
  {:display "block"
   :width "100%"
   :background styles/light-gray
   :border "none"
   :resize "none"
   :font "inherit"
   :font-size "125%"
   :padding "0.75rem"
   :height "8.5rem"
   :border-radius "0.25rem"
   :border-bottom "solid 2px transparent"
   :border-top "solid 2px transparent"
   :overflow-y "auto"
   :&:focus {:outline "none"
             :border-bottom-color styles/main-color}})

(defstyles new-exercise-tag-emoji []
  {:display "flex"
   :justify-content "space-between"})

(defstyles new-exercise-buttons-style []
  {:display "flex"
   :align-items "center"
   :padding "0.5rem"})

(defstyles new-exercise-minutes-label-style []
  {:font-size "150%"
   :font-weight "bold"
   :color styles/gray})

(defstyles new-exercise-minutes-input-style []
  {:border "none"
   :outline "none"
   :background styles/light-gray
   :color styles/gray
   :font-size "200%"
   :text-align "center"
   :width "6rem"
   :padding "0.5rem"
   :border-bottom "solid 2px transparent"
   :border-top "solid 2px transparent"
   :border-radius "0.25rem"
   :font-weight "bold"
   :font-variant-numeric "tabular-nums"
   :&:focus {:border-bottom-color styles/main-color}})

(defstyles new-exercise-minutes-style []
  {:display "flex"
   :align-items "center"
   :flex 1
   "> *" {:margin "0 0.25rem"}
   "> *:first-child" {:margin-left "0px"}
   "&:focus-within .minutes-label" {:color styles/main-color}
   "&:focus-within .minutes-button" {:color styles/main-color}
   "&:focus-within .minutes-input" {:color styles/main-color}})

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
                                                             :tags (:tags @state)}])
        on-pick-emoji (fn [emoji]
                        (update-description (+ (:description @state) (.-colons ^js/Emoji emoji))))]
    (fn []
      [:div {:class (new-exercise-form-style)}
       [:> react-contenteditable {:class (new-exercise-input-style)
                                  :placeholder "How did you exercise?"
                                  :html (parse-emojis (:description @state))
                                  :on-change handle-description-change}]
       [:div {:class (new-exercise-tag-emoji)}
        [exercise-tags {:tags (:tags @state)
                        :on-add add-tag
                        :on-delete delete-tag}]
        [:div
         [emoji-picker {:on-select on-pick-emoji}]]]
       [:div {:class (new-exercise-buttons-style)}
        [:div {:class (new-exercise-minutes-style)}
         [:button {:class (classes (styles/icon-button) "minutes-button")
                                  :type "button"
                                  :on-click #(dec-minutes)}
          [icons/minus]]
         [:input {:class (classes (new-exercise-minutes-input-style) "minutes-input")
                  ;; Even though this input controls a number I don't want to use a number input type,
                  ;; because that would add the DOM's own increment and decrement buttons which I don't want.
                  :type "text"
                  :value (:minutes @state)
                  :on-change handle-minutes-change}]
         [:button {:class (classes (styles/icon-button) "minutes-button")
                   :type "button"
                   :on-click #(inc-minutes)}
          [icons/plus]]
         [:label {:class (classes (new-exercise-minutes-label-style) "minutes-label")
                  :for "minutes"}
          "minutes"]]
        [:button {:class (styles/icon-button-cta)
                  :type "button"
                  :on-click create-exercise}
         "Submit"]]])))

(defstyles exercises-content-style []
  {:flex 1})

(defstyles exercises-exercises-style []
  {:margin "2rem 0 0.25rem 0"})

(defstyles exercise-style []
  {:padding "1rem 0"
   :border-top (str "solid 1px " styles/middle-gray)})

(defstyles exercise-title-style []
  {:display "flex"
   :align-items "center"
   :justify-content "space-between"})

(defstyles exercise-minutes-style []
  {:font-size "300%"
   :font-weight "bold"
   :color styles/main-color-active})

(defstyles exercise-description-style []
  {:font-size "150%"})

(defstyles exercise-adding-style []
  {:border-top (str "solid 1px " styles/middle-gray)
   :padding-top "1rem"})

(defstyles exercise-add-style []
  {:border-top (str "solid 1px " styles/middle-gray)})

(defstyles exercise-add-button-style []
  {:margin-top "0.5rem"
   :margin-left "-0.5rem"
   "&:focus:not(:focus-visible)" {:box-shadow "none !important"
                                  :border (str styles/focus-border-inactive " !important")}})

(defn created-workouts []
  (let [adding (reagent/atom false)
        delete-workout #(dispatch [:delete-workout %])]

    (fn [{:keys [local-date workouts]}]
      [:div {:class (exercises-content-style)}
       [:div {:class (exercises-exercises-style)}
        (map
         (fn [workout]
           ^{:key (:workout_id workout)}
           [:div {:class (exercise-style)}
            [:div {:class (exercise-title-style)}
             [:div {:class (exercise-minutes-style)}
              [:span (str (ms->m (:duration workout)) " minutes")]]
             [:button {:class (styles/icon-button)
                       :on-click #(delete-workout (:workout_id workout))}
              [icons/trash
               [:span " Delete"]]]]
            [:div {:class (exercise-description-style)
                   :dangerouslySetInnerHTML {:__html (parse-emojis (:description workout))}}]
            (when (> (count (:tags workout)) 0)
              [:div (str "tags: " (join ", " (:tags workout)))])])
         workouts)]
       (if @adding
         [:div {:class (exercise-adding-style)}
          [new-workout {:local-date local-date}]]

         [:div {:class (exercise-add-style)}
          [:button {:class (classes (exercise-add-button-style) (styles/icon-button))
                    :on-click #(reset! adding true)}
           [icons/plus-circle
            [:span " Add another"]]]])])))

(defn calculate-total-workout-minutes [workouts]
  (reduce
   (fn [minutes workout]
     (+ minutes (:duration workout)))
   0
   workouts))

(defstyles calendar-year-style []
  {:width "100%"
   :margin "1rem"})

(defstyles calendar-animation-overflow-style []
  {:overflow "hidden"
   :position "relative"
   :height "calc(25rem + 12px)"
   "@media only screen and (max-width: 800px)" {:height "calc(20rem + 12px)"}})

(defstyles calendar-week-style []
  {:display "flex"})

(defstyled day-div :div
  {:position "relative"
   :flex 1
   :display "flex"
   :flex-direction "column"
   :height "5rem"
   :background "rgba(231, 238, 241, 0.4)"
   :margin "2px"
   "@media only screen and (max-width: 800px)" {:height "4rem"}
   :is-today? {:border (str "1px solid " styles/main-color)}
   :is-future? {:opacity "0.33"}
   :no-minutes? {:background "rgba(231, 238, 241)"}})

(defstyles day-date-style []
  {:position "absolute"
   :top 0
   :left 0
   :right 0
   :display "flex"
   :justify-content "space-between"
   :color styles/gray
   :font-size "85%"
   :pointer-events "none"})

(defstyles day-month-style []
  {:text-transform "uppercase"
   :font-weight "bold"
   :margin "0.25rem"
   :color styles/red})

(defstyles day-number-style []
  {:flex 1
   :text-align "right"
   :margin "0.25rem"})

(defstyles day-minutes-style []
  {:flex 1
   :display "flex"
   :align-items "center"
   :justify-content "center"})

(defstyles calendar-add-exercise-button-style []
  {:color styles/gray
   :flex 1
   :height "100%"
   :&:hover {:background "white"
             :cursor "pointer"}
   :&:focus {:outline "none"
             :border styles/focus-border
             :box-shadow styles/focus-shadow}
   :&:active {:color styles/main-color}})

(defstyles calendar-day-duration-style []
  {:font-weight 700
   :color styles/main-color})

; Basically copy-pasted the calendar functionality (and look) from this repo:
; https://github.com/ReactTraining/hooks-workshop

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
       [:div {:class (calendar-year-style)}
        (t/year start-date)]
       [:div#calendar
        [weekdays]
        [:div {:class (calendar-animation-overflow-style)}
         (map-indexed
          (fn [week-index week]
            ^{:key week-index}
            [:div {:class (calendar-week-style)}
             (map-indexed
              (fn [day-index day]
                (let [parsed-date (local-date->date-time (:local-date day))
                      now (t/now)
                      is-today? (is-same-day? parsed-date now)
                      is-future? (is-future? parsed-date now)]
                  ^{:key (:local-date day)}
                  [day-div {:is-today? is-today?
                            :is-future? is-future?
                            :no-minutes? (not (:workouts day))}
                   [:div {:class (day-date-style)}
                    (when (should-show-month? day-index week-index (:local-date day))
                      [:div {:class (day-month-style)}
                       (human-month-short parsed-date)])
                    [:div {:class (day-number-style)}
                     (t/day parsed-date)]]
                                                                ; TODO: display data about the date's activities
                   [:div {:class (day-minutes-style)}
                    [:button {:class (calendar-add-exercise-button-style)
                              :on-click #(edit-day (+ (* week-index days-in-week) day-index))}
                     (if (:workouts day)
                       (let [total-minutes (ms->m (calculate-total-workout-minutes (:workouts day)))]
                         [:div {:class (calendar-day-duration-style)}
                          total-minutes])
                       [icons/plus])]]

                   (when (= editing-index (+ (* week-index days-in-week) day-index))
                     [modal {:title (day-title (:local-date day)) :on-close stop-editing}
                      (if (:workouts day)
                        ^{:key "created-workouts"}
                        [created-workouts {:local-date (:local-date day)
                                           :workouts (:workouts day)}]
                        ^{:key "new-workout"}
                        [new-workout {:local-date (:local-date day)}])])]))
              week)])
          weeks)]]
       [calendar-nav {:show-later (not (is-same-day? start-date (start-of-week (t/now))))
                      :on-earlier-click show-earlier
                      :on-later-click show-later}]])))

(defn displayable-duration [duration]
  (if (= 0 duration)
    "None"
    (as-> (* duration 1000) d
      (humanize/duration d {:number-format str})
      (capitalize d))))

(defstyles duration-card-style []
  {:padding "0.5em"
   :color "whitesmoke"
   :background-color styles/main-color
   :width "17em"
   :height "3.5em"
   :line-height "2.5em"
   :text-align "center"
   :vertical-align "middle"
   :font-family styles/font-family
   :margin-right "0.5em"
   "@media only screen and (max-width: 500px)" {:margin 0
                                                :margin-top "0.5em"}})

(defstyles duration-card-title-style []
  {:margin 0
   :font-size "1em"
   :font-weight 500})

(defstyles duration-card-duration-style []
  {:margin 0})

(defstyles duration-cards-style []
  {:display "flex"
   :justify-content "center"
   :flex-direction "row"
   "@media only screen and (max-width: 500px)" {:flex-direction "column"
                                                :align-items "center"}})

(defn duration-card [{:keys [title duration]}]
  [:div {:class (duration-card-style)}
   [:span {:class (duration-card-title-style)}
    (str title " ")]
   [:span {:class (duration-card-duration-style)}
    (if (nil? duration)
      ;; TODO: small loading indicator
      "..."
      (displayable-duration duration))]])

(defn exercise-stats []
  (dispatch [:fetch-current-week-exercise-duration])
  (dispatch [:fetch-current-month-exercise-duration])

  (fn []
    (let [week-duration @(subscribe [:current-week-exercise-duration])
          month-duration @(subscribe [:current-month-exercise-duration])]
      [:div {:class (duration-cards-style)}
       [duration-card {:duration week-duration
                       :title "This week"}]
       [duration-card {:duration month-duration
                       :title "This month"}]])))

(defn main []
  [:div
   [exercise-stats]
   [calendar]])

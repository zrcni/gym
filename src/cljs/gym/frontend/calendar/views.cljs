(ns gym.frontend.calendar.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [clojure.string :refer [trim blank?]]
            [goog.string.format]
            [gym.util :refer [str-insert includes?]]
            [react-modal]
            [react-contenteditable]
            [cljs-time.core :as t]
            [cljss.core :as css :refer-macros [defstyles]]
            [cljss.reagent :refer-macros [defstyled]]
            [gym.frontend.components.icons :as icons]
            [gym.frontend.components.chip :refer [chip]]
            [gym.frontend.styles :as styles :refer [classes]]
            [gym.frontend.js-interop :refer [get-value]]
            [gym.frontend.components.emoji-picker :refer [emoji-picker]]
            [gym.frontend.components.modal :refer [modal]]
            [gym.frontend.calendar-utils :refer [ms->m
                                                 m->ms
                                                 num-weeks
                                                 days-in-week
                                                 local-date->date-time
                                                 date-time->dd-mm-yyyy
                                                 human-weekday-short
                                                 calculate-start-date
                                                 same-day?
                                                 first-day-of-month?
                                                 human-month-short
                                                 future?]]))

(defstyles weekdays-style []
  {:display "flex"
   :flex 1
   :margin 2
   "> .weekday" {:text-transform "uppercase"
                 :flex 1
                 :text-align "center"
                 :font-weight "bold"
                 :color styles/text-color-secondary
                 :font-size "85%"}})

(def week-num-width "16px")
(def week-num-margin-right "4px")

(defstyles week-num-pad-style []
  {:width week-num-width
   :margin-right week-num-margin-right})

(defn weekdays []
  [:div {:class (weekdays-style)}
   [:div {:class (week-num-pad-style)}]
   [:div.weekday "Mon"]
   [:div.weekday "Tue"]
   [:div.weekday "Wed"]
   [:div.weekday "Thu"]
   [:div.weekday "Fri"]
   [:div.weekday "Sat"]
   [:div.weekday "Sun"]])

(defstyles calendar-nav-style []
  {:display "flex"
   :margin "0.5rem"
   :font-size "85%"})

(defstyles calendar-earlier-later-style [{:keys [theme]}]
  {:&:hover {:background-color (:theme-color theme)}})

(defn calendar-nav [{:keys [show-later on-earlier-click on-later-click]}]
  (let [theme @(subscribe [:theme])]
    [:div {:class (calendar-nav-style)}
     [:button {:on-click on-earlier-click :class (classes (calendar-earlier-later-style {:theme theme})
                                                          (styles/icon-button {:theme theme}))}
      [icons/chevron-up {:class (styles/base-icon)}]
      [:span "Earlier"]]
     (when show-later
       [:button {:on-click on-later-click :class (classes (calendar-earlier-later-style {:theme theme})
                                                          (styles/icon-button {:theme theme}))}
        [icons/chevron-down {:class (styles/base-icon)}]
        [:span "Later"]])]))

(defn first-displayed-day? [day-index week-index]
  (= 0 (+ day-index week-index)))

(defn should-show-month? [day-index week-index local-date]
  (if (or
       (first-displayed-day? day-index week-index)
       (first-day-of-month? (local-date->date-time local-date)))
    true
    false))

(defn day-title [local-date]
  (let [date (local-date->date-time local-date)]
    (as-> (str (human-weekday-short date) " " (date-time->dd-mm-yyyy date)) title
      (if (same-day? date (t/now))
        (str title " - today")
        title))))

(defn to-number
  "Converts a string into a number or just returns the number. Returns 0 if the parsed string converts into NaN"
  [v]
  (if (and (number? v) (not= v js/NaN))
    v
    (let [n (js/parseInt v)]
      (if (= n js/NaN) 0 n))))

(defstyles tag-add-input-style []
  {:border "none"
   :width "100px"
   :height "35px"
   :padding "0 8px 0 8px"
   :color styles/dark-gray
   :background-color styles/light-gray
   :border-top-left-radius "6px"
   :border-bottom-left-radius "6px"
   :&:focus {:outline "none"
             :box-shadow (str "inset 0px 0px 0.15rem " styles/main-color)}})

(defstyles tag-add-button-style [{:keys [theme]}]
  {:border "none"
   :font-size "85%"
   :height "35px"
   :width "35px"
   :padding "2px 8px 0 8px"
   :background-color (:theme-color theme)
   :color styles/text-color
   :border-top-right-radius "6px"
   :border-bottom-right-radius "6px"
   :&:hover {:background-color (:theme-color-hover theme)}})

(defn add-tag [{:keys [on-add]}]
  (let [tag (reagent/atom "")
        add #(when-not (empty? (trim @tag))
               (on-add @tag)
               (reset! tag ""))
        on-key-down #(when (= 13 (.-keyCode %)) (add))]
    (fn []
      (let [theme @(subscribe [:theme])]
        [:div
         [:input {:class (tag-add-input-style)
                  :type "text"
                  :value @tag
                  :placeholder "tags"
                  :on-change #(reset! tag (get-value %))
                  :on-key-down on-key-down}]
         [:button {:class (tag-add-button-style {:theme theme})
                   :type "button"
                   :on-click add}
          [icons/plus]]]))))

(defstyles new-exercise-tags-style []
  {:display "flex"})

(defstyles new-exercise-tags-wrapper-style []
  {:display "flex"
   :align-items "center"
   :margin-left "4px"})

(defn exercise-tags [{:keys [tags on-add on-delete]}]
  [:div {:class (new-exercise-tags-style)}
   [add-tag {:on-add on-add}]
   [:div {:class (new-exercise-tags-wrapper-style)}
    (map (fn [tag]
           [chip {:key tag
                  :value tag
                  :on-delete on-delete}]) tags)]])

(defstyles new-exercise-form-style []
  {:position "relative"
   :margin "0px"
   :flex 1
   :padding-left "16px"
   :padding-right "16px"
   "> *:first-child" {:margin-top "1rem"}})

(defstyles new-exercise-description-input-style [{:keys [theme]}]
  {:display "block"
   :width "100%"
   :color styles/text-color
   :background styles/bg-color-secondary
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
             :border-bottom-color (:theme-color-active theme)}})

(defstyles new-exercise-form-row []
  {:display "flex"
   :margin "16px 0 16px 0"
   :align-items "center"
   :justify-content "space-between"})

(defstyles new-exercise-minutes-label-style []
  {:font-size "150%"
   :font-weight "bold"
   :color styles/gray})

(defstyles new-exercise-minutes-input-style [{:keys [theme]}]
  {:border "none"
   :outline "none"
   :background styles/bg-color-secondary
   :color styles/text-color
   :font-size "200%"
   :text-align "center"
   :width "5.5rem"
   :height "3rem"
   :padding "0.5rem"
   :border-bottom "solid 2px transparent"
   :border-top "solid 2px transparent"
   :border-radius "6px"
   :font-weight "bold"
   :font-variant-numeric "tabular-nums"
   :&:focus {:border-bottom-color (:theme-color-active theme)}})

(defstyles new-exercise-minutes-style []
  {:display "flex"
   :align-items "center"
   :flex 1
   "> *" {:margin "0 0.25rem"}
   "> *:first-child" {:margin-left "0px"}
   "&:focus-within .minutes-label" {:color styles/text-color}
   "&:focus-within .minutes-button" {:color styles/text-color}
   "&:focus-within .minutes-input" {:color styles/text-color}})

(defstyles new-exercise-button-style []
  {:border-radius "12px"
   :height "3rem"})

(defn new-workout [{:keys [local-date]}]
  ;; TODO: persist state, so closing the modal doesn't wipe the data
  (let [theme @(subscribe [:theme])
        caret-pos (atom nil)
        state (reagent/atom {:minutes 30
                             :description ""
                             :tags []})
        min-minutes 0
        update-minutes #(swap! state assoc :minutes %)
        update-description #(swap! state assoc :description %)
        handle-description-change #(update-description (get-value %))
        handle-save-caret-pos #(reset! caret-pos (js/window.getCaretPosition (.-target %)))
        handle-minutes-change #(update-minutes (get-value %))
        inc-minutes #(let [n (to-number (:minutes @state))]
                       (update-minutes (inc n)))
        dec-minutes #(let [n (to-number (:minutes @state))]
                       (when (> n min-minutes)
                         (update-minutes (dec n))))
        add-tag #(when-not (or (includes? (:tags @state) %) (blank? %))
                   (swap! state update :tags conj %))
        delete-tag #(swap! state assoc :tags (filter (fn [tag] (not= tag %)) (:tags @state)))
        create-exercise #(dispatch [:create-workout-request {:date local-date
                                                             :description (:description @state)
                                                             :duration (-> (:minutes @state)
                                                                           (to-number)
                                                                           (m->ms))
                                                             :tags (vec (:tags @state))}])
        on-pick-emoji (fn [_ emoji]
                        (let [emoji-str (.-emoji ^js/Emoji emoji)]
                          (if @caret-pos
                            (do (update-description (str-insert (:description @state) emoji-str @caret-pos))
                                (swap! caret-pos + (count emoji-str)))
                            (update-description (str (:description @state) emoji-str)))))]

    (fn []
      [:div {:class (new-exercise-form-style)}
       [:div {:class (new-exercise-form-row)}
        [:> react-contenteditable {:class (new-exercise-description-input-style {:theme theme})
                                   :placeholder "How was the exercise?"
                                   :html (:description @state)
                                   :on-change handle-description-change
                                   :on-blur handle-save-caret-pos}]]
       [:div {:class (new-exercise-form-row)}
        [exercise-tags {:tags (:tags @state)
                        :on-add add-tag
                        :on-delete delete-tag}]
        [:div
         [emoji-picker {:on-select on-pick-emoji}]]]
       [:div {:class (new-exercise-form-row)}
        [:div {:class (new-exercise-minutes-style)}
         [:button {:class (classes (styles/icon-button {:theme theme}) "minutes-button")
                   :type "button"
                   :on-click #(dec-minutes)}
          [icons/minus]]
         [:input {:class (classes (new-exercise-minutes-input-style {:theme theme}) "minutes-input")
                  :type "text"
                  :value (:minutes @state)
                  :on-change handle-minutes-change}]
         [:button {:class (classes (styles/icon-button {:theme theme}) "minutes-button")
                   :type "button"
                   :on-click #(inc-minutes)}
          [icons/plus]]
         [:label {:class (classes (new-exercise-minutes-label-style) "minutes-label")
                  :for "minutes"}
          "minutes"]]

        [:button {:class (classes (styles/icon-button-cta {:theme theme}) (new-exercise-button-style))
                  :type "button"
                  :on-click create-exercise}
         "Create"]]])))

(defstyles exercises-content-style []
  {:flex 1
   :overflow-y "auto"
   :padding-left "16px"
   :padding-right "16px"
   ::css/media {[:only :screen :and [:max-width "800px"]]
                {:height "calc(100vh - 4rem)"}}})

(defstyles exercises-exercises-style []
  {:margin "0 0 0.25rem 0"})

(defstyles exercise-style []
  {:padding "1rem 0"})

(defstyles exercise-title-style []
  {:color styles/text-color
   :display "flex"
   :align-items "center"
   :justify-content "space-between"})

(defstyles exercise-minutes-style [{:keys [theme]}]
  {:font-size "300%"
   :font-weight "bold"
   :color (:theme-color-active theme)})

(defstyles exercise-description-style []
  {:color styles/text-color
   :font-size "150%"})

(defstyles exercise-adding-style []
  {:border-top (str "solid 1px " styles/middle-gray)
   :padding-top "1rem"})

(defstyles exercise-add-style []
  {:border-top (str "solid 1px " styles/middle-gray)})

(defstyles exercise-add-button-style []
  {:margin-top "0.5rem"
   "&:focus:not(:focus-visible)" {:box-shadow "none !important"
                                  :border (str styles/focus-border-inactive " !important")}})
(defstyles exercise-add-button-text-style []
  {:margin-left "0.5rem"})

(defstyles exercise-tags-wrapper-style []
  {:display "flex"
   :margin-top "0.5rem"
   "> *:first-child" {:margin-left "0px"}})

(defn created-workouts []
  (let [theme @(subscribe [:theme])
        adding (reagent/atom false)
        delete-workout #(dispatch [:delete-workout %])]

    (fn [{:keys [local-date workouts]}]
      [:div {:class (exercises-content-style)}
       [:div {:class (exercises-exercises-style)}
        (map
         (fn [workout]
           [:div {:key (:workout_id workout)
                  :class (exercise-style)}
            [:div {:class (exercise-title-style)}
             [:div {:class (exercise-minutes-style {:theme theme})}
              [:span (str (ms->m (:duration workout)) " minutes")]]
             [:button {:class (styles/icon-button {:theme theme})
                       :on-click #(delete-workout (:workout_id workout))}
              [icons/trash {:class (styles/base-icon)}]]]
            [:div {:class (exercise-description-style)}
             (:description workout)]
            (when (> (count (:tags workout)) 0)
              [:div {:class (exercise-tags-wrapper-style)}
               (map
                (fn [tag] [chip {:key tag
                                 :value tag}])
                (:tags workout))])])
         workouts)]
       (if @adding
         [:div {:class (exercise-adding-style)}
          [new-workout {:local-date local-date}]]

         [:div {:class (exercise-add-style)}
          [:button {:class (classes (styles/icon-button {:theme theme}) (exercise-add-button-style))
                    :on-click #(reset! adding true)}
           [icons/plus-circle {:class (styles/base-icon)}]
           [:span {:class (exercise-add-button-text-style)} "Add another"]]])])))

(defn calculate-total-workout-minutes [workouts]
  (reduce
   (fn [minutes workout]
     (+ minutes (:duration workout)))
   0
   workouts))

(defstyles calendar-animation-overflow-style []
  {:overflow "hidden"
   :position "relative"
   :height "calc(25rem + 18px)"
   ::css/media {[:only :screen :and [:max-width "800px"]]
                {:height "calc(20rem + 18px)"}}})

(defstyles calendar-week-style []
  {:display "flex"})

(defstyled day-div :div
  {:position "relative"
   :flex 1
   :display "flex"
   :flex-direction "column"
   :height "5rem"
   :background styles/main-color-secondary
   :margin "2px"
   ::css/media {[:only :screen :and [:max-width "800px"]]
                {:height "4rem"}}
   :today? {:border (with-meta #(str "1px solid " (:theme-color-active %1)) [:theme])}
   :future? {:opacity "0.33"}})

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

(defstyles calendar-add-exercise-button-style [{:keys [theme]}]
  {:color styles/middle-gray
   :flex 1
   :height "100%"
   :&:hover {:background (:theme-color theme)
             :cursor "pointer"
             :color styles/text-color}
   :&:focus {:outline "none"
             :border styles/focus-border
             :box-shadow styles/focus-shadow
             :color styles/text-color}
   :&:active {:color (:theme-color-active theme)}
   :&:disabled {:cursor "not-allowed"
                :border "none"
                :box-shadow "none"
                :background "inherit"
                :color "inherit"}})

(defstyles calendar-day-duration-style []
  {:font-weight 700
   :color styles/text-color})

(defstyles week-num-wrapper-style []
  {:width week-num-width
   :margin-right week-num-margin-right
   :display "flex"
   :align-items "center"
   :justify-content "flex-end"})

(defstyles week-num-style []
  {:text-align "center"
   :font-size "0.85rem"})

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
  (let [theme @(subscribe [:theme])
        start-date @(subscribe [:calendar-start-date])
        editing-index @(subscribe [:calendar-editing-index])
        weeks @(subscribe [:calendar-weeks])
        loading @(subscribe [:calendar-loading])
        handle-edit-day #(dispatch [:calendar-edit-day %])
        handle-stop-editing #(dispatch [:calendar-stop-editing])
        handle-show-earlier #(dispatch [:calendar-show-earlier (t/days (* num-weeks days-in-week))])
        handle-show-later #(dispatch [:calendar-show-later (t/days (* num-weeks days-in-week))])]

    [:div
     [:div#calendar
      [weekdays]
      [:div {:class (calendar-animation-overflow-style)}
       (map-indexed
        (fn [week-index week]
          [:div {:key week-index
                 :class (calendar-week-style)}
           [:div {:class (week-num-wrapper-style)}
            [:span {:class (week-num-style)}
             (:week-num week)]]

           (map-indexed
            (fn [day-index day]
              (let [parsed-date (local-date->date-time (:local-date day))
                    now (t/now)
                    is-today (same-day? parsed-date now)
                    is-future (future? parsed-date now)]

                (if is-future
                  [day-div {:key (:local-date day)
                            :today? is-today
                            :future? is-future}
                   [:button {:class (calendar-add-exercise-button-style {:theme theme})
                             :disabled true}
                    [icons/plus]]]

                  [day-div {:key (:local-date day)
                            :today? is-today
                            :future? is-future}
                   [:div {:class (day-date-style)}
                    (when (should-show-month? day-index week-index (:local-date day))
                      [:div {:class (day-month-style)}
                       (human-month-short parsed-date)])
                    [:div {:class (day-number-style)}
                     (t/day parsed-date)]]
                                                                ; TODO: display data about the date's activities
                   [:div {:class (day-minutes-style)}
                    [:button {:class (calendar-add-exercise-button-style {:theme theme})
                              :on-click #(handle-edit-day (+ (* week-index days-in-week) day-index))
                              :disabled loading}
                     (if (:workouts day)
                       (let [total-minutes (ms->m (calculate-total-workout-minutes (:workouts day)))]
                         [:div {:class (calendar-day-duration-style)}
                          total-minutes])
                       [icons/plus])]]

                   (when (= editing-index (+ (* week-index days-in-week) day-index))
                     [modal {:title (day-title (:local-date day)) :on-close handle-stop-editing}
                      (if (:workouts day)
                        [created-workouts {:key "created-workouts"
                                           :local-date (:local-date day)
                                           :workouts (:workouts day)}]
                        [new-workout {:key "new-workout"
                                      :local-date (:local-date day)}])])])))
            (:days week))])
        weeks)]]
     [calendar-nav {:show-later (not (same-day? start-date (calculate-start-date (t/now) num-weeks)))
                    :on-earlier-click handle-show-earlier
                    :on-later-click handle-show-later}]]))

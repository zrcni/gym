(ns gym.home.duration-cards.views
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [clojure.string :refer [capitalize]]
   [goog.string.format]
   [react-modal]
   [react-contenteditable]
   [emojiMart]
   [smileParser]
   [clojure.contrib.humanize :as humanize]
   [gym.styles :as styles]
   [cljss.core :as css :refer-macros [defstyles]]
   [gym.home.duration-cards.subs :as subs]
   [gym.home.duration-cards.events :as events]
   [gym.components.loaders :as loaders]))

(defn displayable-duration [duration]
  (if (= 0 duration)
    "None"
    (as-> (* duration 1000) d
      (humanize/duration d {:number-format str})
      (capitalize d))))

(defstyles duration-cards-style []
  {:display "flex"
   :justify-content "center"
   :flex-direction "row"
   ::css/media {[:only :screen :and [:max-width "800px"]]
                {:flex-direction "column"
                 :align-items "center"}}})

(defstyles duration-card-style []
  {:display "grid"
   :grid-template-columns "7rem 11rem"
   :grid-template-rows "3rem"
   :color styles/text-color
   :background-color styles/main-color
   :height "3rem"
   :line-height "3rem"
   :text-align "center"
   :vertical-align "middle"
   :font-family styles/font-family
   :margin-right "0.5em"
   :border-radius "6px"
   :white-space "nowrap"
   :text-overflow "ellipsis"
   :overflow "hidden"
   ::css/media {[:only :screen :and [:max-width "800px"]]
                {:margin 0
                 :margin-top "0.5em"}}})

(defstyles duration-card-title-style [{:keys [theme]}]
  {:margin 0
   :font-size "1em"
   :font-weight 500
   :background (:theme-color theme)
   :border-top-left-radius "6px"
   :border-bottom-left-radius "6px"})

(defstyles duration-card-duration-style []
  {:display "flex"
   :align-items "center"
   :justify-content "center"
   :margin 0
   :font-weight 500
   :border-top-right-radius "6px"
   :border-bottom-right-radius "6px"})

(defn duration-card [{:keys [title duration loading]}]
  (let [theme @(subscribe [:theme])]
    [:div {:class (duration-card-style)}
     [:div {:class (duration-card-title-style {:theme theme})}
      [:span
       (str title " ")]]
     [:div {:class (duration-card-duration-style)}
      (if loading
        [loaders/circle]
        [:span
         (if (nil? duration)
           ":("
           (displayable-duration duration))])]]))

(defn duration-cards []
  (dispatch [::events/fetch-current-week-exercise-duration])
  (dispatch [::events/fetch-current-month-exercise-duration])

  (fn []
    (let [week-duration @(subscribe [::subs/week-exercise-duration])
          month-duration @(subscribe [::subs/month-exercise-duration])
          loading @(subscribe [::subs/exercise-duration-loading])]
      [:div {:class (duration-cards-style)}
       [duration-card {:duration month-duration
                       :title "This month"
                       :loading loading}]
       [duration-card {:duration week-duration
                       :title "This week"
                       :loading loading}]])))

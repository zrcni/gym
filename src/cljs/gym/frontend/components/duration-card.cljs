(ns gym.frontend.components.duration-card
  (:require
   [re-frame.core :refer [subscribe]]
   [clojure.string :refer [capitalize]]
   [clojure.contrib.humanize :as humanize]
   [gym.frontend.styles :as styles]
   [cljss.core :as css :refer-macros [defstyles]]
   [gym.frontend.components.loaders :as loaders]))

(defn human-duration [duration]
  (if (= 0 duration)
    "None"
    (as-> duration d
      (humanize/duration d {:number-format str})
      (capitalize d))))

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

(defn duration-card [{:keys [title duration loading?]}]
  (let [theme @(subscribe [:theme])]
    [:div {:class (duration-card-style)}
     [:div {:class (duration-card-title-style {:theme theme})}
      [:span
       (str title " ")]]
     [:div {:class (duration-card-duration-style)}
      (if loading?
        [loaders/circle]
        [:span
         (if (nil? duration)
           ":("
           (human-duration duration))])]]))

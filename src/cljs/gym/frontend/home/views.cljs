(ns gym.frontend.home.views
  (:require [re-frame.core :refer [dispatch subscribe]]
            [cljs-time.core :as t]
            [cljss.core :as css :refer-macros [defstyles]]
            [gym.frontend.duration-cards.views :refer [duration-cards]]
            [gym.frontend.calendar.views :refer [calendar]]))

(defstyles calendar-header-style []
  {:margin-bottom "1.3rem"
   :margin-top "0.65rem"
   :align-self "flex-end"
   :display "grid"
   :grid-template-columns "5rem auto 5rem"})

(defstyles calendar-year-style []
  {:align-self "center"
   :justify-self "center"
   :font-size "1.3rem"})



(defn main []
  (dispatch [:fetch-all-workouts])

  (fn []
    (let [start-date @(subscribe [:calendar-start-date])]
      [:div
       [:div {:class (calendar-header-style)}
        [:div {:class (calendar-year-style)}
         (t/year start-date)]
        [duration-cards]]
       [calendar]])))

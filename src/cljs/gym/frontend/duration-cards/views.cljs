(ns gym.frontend.duration-cards.views
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [cljss.core :as css :refer-macros [defstyles]]
   [gym.frontend.components.duration-card :refer [duration-card]]))

(defstyles duration-cards-style []
  {:display "flex"
   :justify-content "center"
   :flex-direction "row"
   ::css/media {[:only :screen :and [:max-width "800px"]]
                {:flex-direction "column"
                 :align-items "center"}}})

(defn duration-cards-impl [{:keys [loading?]}]
  (let [week-result @(subscribe [:analytics-query :workout-duration-this-week])
        month-result @(subscribe [:analytics-query :workout-duration-this-month])]

    [:div {:class (duration-cards-style)}
     [duration-card {:duration (-> month-result :data :duration)
                     :title "This month"
                     :loading? (or loading?
                                   (and (:loading month-result)
                                        (not month-result)))}]
     [duration-card {:duration (-> week-result :data :duration)
                     :title "This week"
                     :loading? (or loading?
                                   (and (:loading week-result)
                                        (not week-result)))}]]))



(defn duration-cards []
  (let [excluded-tags @(subscribe [:excluded-tags])
        params (when-not (empty? excluded-tags) {:exclude excluded-tags})
        init-loading? @(subscribe [:loading :fetch-initial-user-prefs])]

    (when-not init-loading?
      (dispatch [:analytics-query [:workout-duration-this-week params]])
      (dispatch [:analytics-query [:workout-duration-this-month params]])))

  [duration-cards-impl])

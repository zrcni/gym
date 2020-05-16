(ns gym.views
  (:require
   [gym.login.events]
   [gym.components.icons :as icons]
   [gym.styles :as styles :refer [classes]]
   [cljss.core :refer-macros [defstyles]]
   [re-frame.core :refer [subscribe dispatch]]))

(defstyles header-title-style []
  {:color "white"})

(defstyles header-left-style []
  {:display "flex"
   :justify-content "flex-start"
   :align-items "center"})

(defstyles header-right-style []
  {:display "flex"
   :justify-content "flex-end"
   :align-items "center"})

(defstyles logout-button-style []
  {:background-color styles/main-color
   :border-color "#900c3f"
   :color "#fff"
   :margin-left "4px"
   :margin-right "4px"
   :padding "4px"
   :&:hover {:color styles/middle-gray}})

(defstyles header-style []
  {:display "flex"
   :padding "8px"
   :height "3em"
   :background-color styles/main-color
   :justify-content "space-between"
   :font-family styles/font-family
   :font-weight 500})

(defstyles content-style []
  {:padding "12px"})

(defn layout []
  (fn [_ & children]
    (let [user @(subscribe [:user])]
      [:<>
       [:header#header {:class (classes (header-style) "navbar navbar-expand navbar-dark flex-md-row bd-navbar")}
        [:div {:class (header-left-style)}
         [:a {:class (header-title-style)
              :href "/"} "Exercise tracker"]]
        [:div {:class (header-right-style)}
         (when user
           [:button {:class (logout-button-style)
                     :on-click #(dispatch [::gym.login.events/logout])}
            [icons/power-off]])]]
       [:main#content {:class (content-style)}
        children]])))

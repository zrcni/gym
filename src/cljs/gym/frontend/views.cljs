(ns gym.frontend.views
  (:require
   [gym.frontend.login.events]
   [gym.frontend.components.dropdown-menu :refer [dropdown-menu dropdown-item dropdown-button]]
   [gym.frontend.components.icons :as icons]
   [gym.frontend.styles :as styles :refer [classes]]
   [cljss.core :refer-macros [defstyles]]
   [re-frame.core :refer [subscribe dispatch]]))

(defstyles header-title-style [{:keys [theme]}]
  {:color styles/text-color
   :margin-right "1em"
   :&:hover {:color (:theme-color-active theme)}})

(defstyles header-left-style []
  {:display "flex"
   :justify-content "flex-start"
   :align-items "center"})

(defstyles header-right-style []
  {:display "flex"
   :justify-content "flex-end"
   :align-items "center"})

(defstyles logout-button-style []
  {:background-color "darkred"
   :&:hover {:background-color "red"}
   :&:active {:color styles/text-color}})

(defstyles header-style []
  {:display "flex"
   :padding "8px"
   :height "3em"
   :background-color styles/main-color
   :justify-content "space-between"
   :font-family styles/font-family
   :font-weight 500})

(defstyles content-style []
  {:padding "12px"
   :max-width "60rem"
   :margin "0 auto"})

(defn layout []
  (fn [_ & children]
    (let [theme @(subscribe [:theme])
          user @(subscribe [:user])]
      [:<>
       [:header#header {:class (classes (header-style) "navbar navbar-expand navbar-dark flex-md-row bd-navbar")}
        [:div {:class (header-left-style)}
         [:a {:class (header-title-style {:theme theme})
              :href "/"}
          "Exercise tracker"]
         [:a {:class (header-title-style {:theme theme})
              :href "/analytics"}
          "Analytics"]]
        [:div {:class (header-right-style)}
         [dropdown-menu
          [dropdown-button {:on-click #(dispatch [:navigate :settings])
                            :key "settings"}
           [icons/cog {:class (styles/base-icon)
                       :key "settings-icon"}]
           [:span {:key "settings-label"} " Settings"]]
          (when user
            [dropdown-button {:class (logout-button-style)
                              :on-click #(dispatch [::gym.frontend.login.events/logout])
                              :key "logout"}
             [icons/power-off {:class (styles/base-icon)
                               :key "logout-icon"}]
             [:span {:key "logout-label"} " Log out"]])]]]
       [:main#content {:class (content-style)}
        children]])))

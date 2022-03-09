(ns gym.frontend.settings.views
  (:require
   [gym.frontend.login.events]
   [gym.frontend.theme]
   [re-frame.core :refer [subscribe dispatch]]
   [gym.frontend.styles :as styles :refer [classes]]
   [cljss.core :refer-macros [defstyles]]
   [gym.frontend.components.chip :refer [chip-button]]
   [gym.frontend.components.color-picker :refer [color-picker]]))

(defstyles color-picker-style []
  {:display "flex"
   :align-items "center"
   :margin "8px"})

(defstyles save-button-style []
  {:margin "8px"
   :width "4rem"
   :height "2rem"
   :padding 0
   :font-size "1rem"
   :text-align "center"
   :display "block"})

(defstyles wrapper-style []
  {:display "flex"
   :align-items "flex-end"})

(defstyles setting-row-style []
  {:margin-bottom "16px"})

(defstyles preview-description-style []
  {:margin-left "0.5rem"
   :margin-top "0.25rem"})

(defn theme-color []
  (let [theme @(subscribe [:theme])]
    [:div {:class (setting-row-style)}
     [:h4 "Theme color"]
     [:div
      [:div {:class (wrapper-style)}
       [color-picker {:class (color-picker-style)
                      :color (:theme-color theme)
                      :on-change #(dispatch [::gym.frontend.theme/update-theme-color (.-hex ^js/Color %)])}]]
      
      (when (:preview? theme)
        [:p {:class (preview-description-style)}
         "Theme color is not saved yet, but you can preview it in other parts of the application."])]]))

(defstyles chip-btn-excluded []
  {:background-color styles/dark-gray})

(defn exclude-tags []
  (let [all-tags @(subscribe [:all-tags])
        excluded-tags @(subscribe [:excluded-tags])
        toggle-tag-exclusion #(dispatch [:toggle-excluded-tag %])]
    [:div {:class (setting-row-style)}
     [:h4 "Tags used in all analytics"]
     [:div
      [:div {:class (wrapper-style)}
       (map (fn [tag]
              [chip-button {:key tag
                            :value tag
                            :on-click #(toggle-tag-exclusion tag)
                            :class (when (some #(= % tag) excluded-tags)
                                     (chip-btn-excluded))}])
            all-tags)]]]))



(defn main []
  (dispatch [:fetch-all-workout-tags])

  (fn [_]
    (let [theme @(subscribe [:theme])
          handle-click-save #(dispatch [:save-user-prefs])]
      [:div#settings
       [theme-color]
       [exclude-tags]
       [:button {:class (classes (styles/icon-button-cta {:theme theme}) (save-button-style))
                 :on-click handle-click-save}
        "Save"]])))

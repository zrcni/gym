(ns gym.frontend.settings.views
  (:require
   [gym.frontend.login.events]
   [gym.frontend.theme]
   [re-frame.core :refer [subscribe dispatch]]
   [gym.frontend.styles :as styles :refer [classes]]
   [cljss.core :refer-macros [defstyles]]
   [gym.frontend.components.color-picker :refer [color-picker]]))

(defstyles loader-wrapper-style []
  {:margin-top "1rem"
   :justify-content "center"
   :display "flex"
   :flex-direction "column"
   :align-items "center"})

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
                      :on-change #(dispatch [::gym.frontend.theme/update-theme-color (.-hex ^js/Color %)])}]

       [:button {:class (classes (styles/icon-button-cta {:theme theme}) (save-button-style))
                 :on-click #(dispatch [::gym.frontend.theme/persist-theme])
                 :disabled (not (:preview? theme))}
        "Save"]]
      
      (when (:preview? theme)
        [:p {:class (preview-description-style)}
         "Theme color is not saved yet, but you can preview it in other parts of the application."])]]))

(defn main []
  [:div#settings
   [theme-color]])

;; [:div {:class (loader-wrapper-style)}
;;  [loaders/circle {:size 80}]]

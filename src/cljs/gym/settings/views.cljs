(ns gym.settings.views
  (:require
   [gym.login.events]
   [gym.theme]
   [re-frame.core :refer [subscribe dispatch]]
   [gym.styles :as styles :refer [classes]]
   [cljss.core :refer-macros [defstyles]]
   [gym.components.color-picker :refer [color-picker]]))

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

(defn main []
  (let [theme @(subscribe [:theme])]
     [:div
      [:h4 "Accent color"]
      [:div {:class (wrapper-style)}
       [color-picker {:class (color-picker-style)
                      :color (:accent-color theme)
                      :on-change #(dispatch [::gym.theme/update-accent-color (.-hex ^js/Color %)])}]
       [:button {:class (classes (styles/icon-button-cta {:theme theme}) (save-button-style))}
        "Save"]]]))

;; [:div {:class (loader-wrapper-style)}
;;  [loaders/circle {:size 80}]]

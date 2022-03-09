(ns gym.frontend.components.chip
  (:require [re-frame.core :refer [subscribe]]
            [react-modal]
            [react-contenteditable]
            [cljss.core :as css :refer-macros [defstyles]]
            [gym.frontend.components.icons :as icons]
            [gym.frontend.styles :as styles :refer [classes]]))


(defstyles chip-delete-style [{:keys [theme]}]
  {:display "flex"
   :justify-content "center"
   :align-items "center"
   :font-size "20px"
   :cursor "pointer"
   :width "35px"
   :height "35px"
   :&:hover {:background-color (:theme-color-hover theme)
             :border-top-right-radius "6px"
             :border-bottom-right-radius "6px"}})

(defstyles chip-content-style []
  {:padding "0 8px 0 8px"})

(defstyles chip-container-style [{:keys [theme]}]
  {:box-sizing "border-box"
   :color styles/text-color
   :display "flex"
   :align-items "center"
   :height "35px"
   :line-height "20px"
   :white-space "nowrap"
   :background (:theme-color theme)
   :border-radius "6px"
   :cursor "default"
   :opacity 1
   :transition "all 0.3s cubic-bezier(0.78, 0.14, 0.15, 0.86)"
   :margin "0 4px 0 4px"})

(defn chip [{:keys [value on-delete]}]
  (let [theme @(subscribe [:theme])]
    [:div {:class (chip-container-style {:theme theme})}
     [:div {:class (chip-content-style)}
      value]
     (when on-delete
       [:button {:class (classes (chip-delete-style {:theme theme}) "chip-delete")
                 :role "button"
                 :aria-label "delete"
                 :on-click #(on-delete value)}
        [icons/times]])]))


(defstyles chip-button-style []
  {:padding "0 8px 0 8px"
   :height "100%"
   :font-size "16px"})

(defn chip-button [{:keys [value on-click class]}]
  (let [theme @(subscribe [:theme])]
    [:div {:class (chip-container-style {:theme theme})}
     [:button {:on-click on-click
               :class (classes (styles/icon-button-cta {:theme theme})
                               (chip-button-style)
                               class)}
      value]]))

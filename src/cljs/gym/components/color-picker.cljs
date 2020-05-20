(ns gym.components.color-picker
  (:require
   [cljss.core :refer-macros [defstyles]]
   [reagent.core :as reagent]
   [react-color :refer [PhotoshopPicker]]))

(defstyles picker-style []
  {:position "absolute"
   :display "block"
   :z-index 1
   :top 0
   :left 0})

(defstyles button-style [color]
  {:border "none"
   :background-color color
   :width "60px"
   :height "40px"})

(defn color-picker []
  (let [open? (reagent/atom false)
        open-picker #(when-not @open? (reset! open? true))
        close-picker #(when @open? (reset! open? false))]
    (fn [{:keys [color on-change class]}]
      [:div {:class class}
       [:button {:on-click open-picker
                 :class (button-style color)}]
       (when @open?
         [:> PhotoshopPicker {:class (picker-style)
                              :color color
                              :on-change on-change
                              :on-cancel close-picker
                              :on-accept close-picker}])])))

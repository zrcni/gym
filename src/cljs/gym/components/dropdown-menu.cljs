(ns gym.components.dropdown-menu
  (:require
   [re-frame.core :refer [subscribe]]
   [reagent.core :as reagent]
   [gym.login.events]
   [gym.components.icons :as icons]
   [gym.styles :as styles :refer [classes]]
   [cljss.reagent :refer-macros [defstyled]]
   [cljss.core :refer-macros [defstyles]]))

(defstyles ^:private dropdown-menu-style []
  {:position "relative"
   :display "inline-block"
   "&:hover .dropdown-content" {:display "block"}})

(defstyles ^:private dropdown-content-style [{:keys [theme]}]
  {:display "block"
   :position "absolute"
   :background-color styles/bg-color
   :min-width "160px"
   :box-shadow (str "0px 4px 8px 0px " (:accent-color theme))
   :z-index 1
   :right 0})

(defstyles ^:private dropdown-button-style []
  {:width "100%"
   :height "100%"
   :padding "12px 16px"
   :text-align "start"})

(defstyled ^:private dropdown-item :div
  {:color styles/text-color
   :width "100%"
   :height "100%"
   :text-decoration "none"
   :display "block"
   :&:hover {:background-color styles/bg-color-secondary}})

(defn dropdown-menu []
  (let [open? (reagent/atom false)
        open-menu #(when-not @open? (reset! open? true))
        close-menu #(when @open? (reset! open? false))
        on-key-down #(when (and @open? (= 27 (.-keyCode %))) (close-menu))
        on-click-anywhere #(when @open? (close-menu))]

    (reagent/create-class
     {:component-did-mount
      (fn []
        (.addEventListener js/window "keydown" on-key-down)
        (.addEventListener js/document "mouseup" on-click-anywhere))

      :component-will-unmount
      (fn []
        (.removeEventListener js/window "keydown" on-key-down)
        (.removeEventListener js/document "mouseup" on-click-anywhere))

      :reagent-render
      (fn [& children]
        (let [theme @(subscribe [:theme])]
          [:div {:class (dropdown-menu-style)}
           [:button {:class (styles/icon-button {:theme theme})
                     :on-click open-menu}
            [icons/bars]]
           (when @open?
             [:div {:class (classes (dropdown-content-style {:theme theme}) "dropdown-content")}
              (into [dropdown-item children])])]))})))

;; TODO: optional props
;; currently it's required to pass {} as props for children to be rendered
;; TODO: pass rest of the props instead of handling each one, how tho?
;; (defn dropdown-button [{:keys [on-click disabled class]} & children]
;;   [dropdown-item
;;    [:button {:class (classes (dropdown-button-style) class)
;;              :on-click on-click
;;              :disabled disabled}
;;     children]])

(defn dropdown-button [{:keys [on-click disabled class]} & children]
  [:button {:class (classes (dropdown-button-style) class)
            :on-click on-click
            :disabled disabled}
   children])

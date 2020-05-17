(ns gym.components.modal
  (:require
   [cljss.core :as css :refer-macros [defstyles]]
   [gym.styles :as styles]
   [gym.components.icons :as icons]
   [react-modal]))

(.setAppElement js/ReactModal "#app")

(defstyles modal-overlay-style []
  {:position "fixed"
   :top 0
   :left 0
   :right 0
   :bottom 0
   :background "none"
   :backdrop-filter "blur(3px)"})

(defstyles modal-content-style []
  {:margin "10vh auto"
   :width "800px"
   :padding "0px"
   :position "initial"
   :background styles/bg-color
   :overflow "auto"
   :border-radius "6px"
   :outline "none"
   ::css/media {[:only :screen :and [:max-width "800px"]]
                {:width "100%"}
                [:only :screen :and [:max-height "400px"]]
                {:margin 0}}})

(defstyles modal-close-button-style []
  {:background "none"
   :border "none"
   :color styles/text-color
   :padding "0 6px"
   :&:hover {:color styles/middle-gray}})

(defstyles modal-title-style []
  {:padding "12px"
   :font-weight 500
   :background-color styles/main-color
   :color styles/text-color
   :display "flex"
   :justify-content "space-between"})

(defstyles modal-content-container-style []
  {:overflow-y "auto"
   :max-height "600px"})

(defstyles modal-container-style []
  {:width "100%"
   :background-color styles/bg-color
   :padding-right "16px"
   :padding-left "16px"
   :margin-right "auto"
   :margin-left "auto"})

(defn modal []
  (fn [{:keys [disable-auto-close is-open on-close title]} & children]
    [:> js/ReactModal {:is-open (if (nil? is-open) true is-open)
                       :on-request-close #(when-not (nil? on-close) (on-close))
                       :content-label title
                       :should-close-on-overlay-click (not disable-auto-close)
                       :overlay-class-name (modal-overlay-style)
                       :class (modal-content-style)}
     [:div {:class (modal-container-style)}
      (when title
        [:div.row {:class (modal-title-style)}
         [:span title]
         (when-not disable-auto-close
           [:button {:class (modal-close-button-style)
                     :on-click #(when-not (nil? on-close) (on-close))}
            [icons/times]])])
      [:div {:class (modal-content-container-style)}
       children]]]))

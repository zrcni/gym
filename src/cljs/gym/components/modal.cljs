(ns gym.components.modal
  [:require
   [cljss.core :refer-macros [defstyles]]
   [gym.styles :as styles]
   [gym.components.icons :as icons]
   [react-modal]])

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
   :background "white"
   :overflow "auto"
   :border-radius "4px"
   :outline "none"
   "@media only screen and (max-width: 800px)" {:width "100%"}
   "@media only screen and (max-height: 400px)" {:margin 0}})

(defstyles modal-close-button-style []
  {:background "none"
   :border "none"
   :color "white"})

(defstyles modal-title-style []
  {:padding "12px"
   :background-color styles/main-color
   :color "white"
   :display "flex"
   :justify-content "space-between"})

(defstyles modal-container-style []
  {:padding "0 16px 16px 16px"
   :overflow-y "auto"
   :max-height "600px"})

(defn modal []
  (fn [{:keys [disable-auto-close is-open on-close title]} & children]
    [:> js/ReactModal {:is-open (if (nil? is-open) true is-open)
                       :on-request-close #(when-not (nil? on-close) (on-close))
                       :content-label title
                       :should-close-on-overlay-click (not disable-auto-close)
                       :overlay-class-name (modal-overlay-style)
                       :class (modal-content-style)}
     [:div.container-fluid
      (when title
        [:div.row {:class (modal-title-style)}
         [:span title]
         (when-not disable-auto-close
           [:button {:class (modal-close-button-style)
                     :on-click #(when-not (nil? on-close) (on-close))}
            [icons/times]])])
      [:div {:class (modal-container-style)}
       children]]]))

(ns gym.frontend.components.modal
  (:require
   [re-frame.core :refer [subscribe]]
   [cljss.core :as css :refer-macros [defstyles]]
   [gym.frontend.styles :as styles]
   [gym.frontend.components.icons :as icons]
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
                {:width "100%"
                 :height "100vh"
                 :margin 0}}})

(defstyles modal-header-style []
  {:padding "8px"
   :height "3rem"
   :font-weight 500
   :background-color styles/main-color
   :color styles/text-color
   :display "flex"
   :justify-content "space-between"
   :align-content "center"
   :align-items "center"})

(defstyles modal-title-style []
  {:padding-left "0.5rem"})

(defstyles modal-content-container-style []
  {:max-height "600px"
   :overflow-y "auto"
   :padding-bottom "16px"
   ::css/media {[:only :screen :and [:max-width "800px"]]
                {:max-height "initial"}}})

(defstyles modal-container-style []
  {:overflow "hidden"
   :width "100%"
   :background-color styles/bg-color
   :margin-right "auto"
   :margin-left "auto"})

(defn modal []
  (fn [{:keys [disable-auto-close is-open on-close title]} & children]
    (let [theme @(subscribe [:theme])]
      [:> js/ReactModal {:is-open (if (nil? is-open) true is-open)
                         :on-request-close #(when-not (nil? on-close) (on-close))
                         :content-label title
                         :should-close-on-overlay-click (not disable-auto-close)
                         :overlay-class-name (modal-overlay-style)
                         :class (modal-content-style)}
       [:div {:class (modal-container-style)}
        (when title
          [:div {:class (modal-header-style)}
           [:div {:class (modal-title-style)}
            [:span title]]
           (when-not disable-auto-close
             [:button {:class (styles/icon-button {:theme theme})
                       :on-click #(when-not (nil? on-close) (on-close))}
              [icons/times {:class (styles/base-icon)}]])])
        [:div {:class (modal-content-container-style)}
         children]]])))

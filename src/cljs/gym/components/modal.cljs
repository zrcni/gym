(ns gym.components.modal
  (:require [react-modal]))

(.setAppElement js/ReactModal "#app")

(defn modal []
  (fn [{:keys [disable-auto-close is-open on-close title]} & children]
    [:> js/ReactModal {:is-open (if (nil? is-open) true is-open)
                       :on-request-close #(when-not (nil? on-close) (on-close))
                       :content-label title
                       :should-close-on-overlay-click (not disable-auto-close)
                       :overlay-class-name "modal-overlay-custom"
                       :class "modal-content-custom"}
     [:div.container-fluid
      (when title
        [:div.row.modal-title
         [:span title]
         (when-not disable-auto-close
           [:button.modal-close-button {:on-click #(when-not (nil? on-close) (on-close))}
            [:i.fas.fa-times]])])
      [:div {:style {:margin 8}}
       children]]]))

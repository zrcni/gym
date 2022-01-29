(ns gym.frontend.components.emoji-picker
 (:require
  [reagent.core :as reagent]
  [re-frame.core :refer [subscribe]]
  [gym.frontend.dom-utils :refer [parent-of?]]
  [gym.frontend.styles :as styles :refer [classes]]
  [emoji-picker-react]
  [cljss.core :refer-macros [defstyles]]))

(defstyles emoji-picker-button-style []
  {:padding "4px"})

(defn emoji-picker []
  (let [!prev-el (atom nil)
        !el (atom nil)
        state (reagent/atom {:open false
                             :pos {:right 0 :top 0}})
        handle-open-picker (fn [e]
                             (when-not (:open state)
                               (let [body-rect (.getBoundingClientRect js/document.body)
                                     btn-rect (.getBoundingClientRect (.-target e))
                                     right (- (.-right body-rect) (.-right btn-rect))
                                     top (.-top btn-rect)]
                                 (swap! state assoc :pos {:right right
                                                          :top top})
                                 (swap! state assoc :open true))))
        handle-close-picker #(swap! state assoc :open false)
        handle-key-down #(when (and (:open state) (= 27 (.-keyCode %))) (handle-close-picker))
        handle-click-away #(when-not (parent-of? (.-target %) @!el) (handle-close-picker))]

    (reagent/create-class
     {:component-did-mount
      (fn []
        (.addEventListener js/window "keydown" handle-key-down))

      :component-will-unmount
      (fn []
        (.removeEventListener js/window "keydown" handle-key-down)
        (.removeEventListener js/document "mouseup" handle-click-away))

      :component-did-update
      (fn []
        ;; Add clickaway listener whenever the picker is rendered for the first time
        ;; AFAIK no need to remove it because the element is removed
        (when (and @!el (nil? @!prev-el))
          (.addEventListener js/document "mouseup" handle-click-away))
        (reset! !prev-el @!el))

      :reagent-render
      (fn [{:keys [on-select]}]
        (let [theme @(subscribe [:theme])]

          [:<>
           (when (:open @state)
             [:div#emoji-picker {:ref #(reset! !el %)
                                 :style {:position "fixed"
                                         :right (str (-> @state :pos :right) "px")
                                         :top (str (-> @state :pos :top) "px")}}
              [:> emoji-picker-react {:onEmojiClick on-select
                                      :native true}]])

           [:button {:class (classes (styles/icon-button-cta {:theme theme}) (emoji-picker-button-style))
                     :on-click handle-open-picker}
            "🤪"]]))})))

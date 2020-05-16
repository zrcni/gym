(ns gym.components.emoji-picker
  [:require
   [reagent.core :as reagent]
   [cljss.core :refer-macros [defstyles]]
   [emojiMart]
   [smileParser]])

(defn parse-emojis [str]
  (.smileParse smileParser str (clj->js {:url "/img/emojis/"
                                         :styles "height: 1.2em;"})))

(defn parent-of? [el parent]
  (if-not (.-parentNode el)
    false
    (if (= (.-parentNode el) parent)
      true
      (parent-of? (.-parentNode el) parent))))

(defstyles emoji-picker-button-style []
  {:margin "4px"
   :padding "4px"
   :padding-bottom "0px"})

(defn emoji-picker []
  (let [!prev-el (atom nil)
        !el (atom nil)
        state (reagent/atom {:open false
                             :pos {:right 0 :top 0}})
        open-picker (fn [e]
                      (let [body-rect (.getBoundingClientRect js/document.body)
                            btn-rect (.getBoundingClientRect (.-target e))
                            right (- (.-right btn-rect) (.-right body-rect))
                            top (- (.-top btn-rect) (.-top body-rect))]
                        (swap! state assoc :pos [right top])
                        (swap! state assoc :open true)))
        close-picker #(swap! state assoc :open false)
        on-key-down #(when (and (:open state) (= 27 (.-keyCode %))) (close-picker))]

    (reagent/create-class
     {:component-did-mount
      (fn []
        (.addEventListener js/window "keydown" on-key-down))

      :component-will-unmount
      (fn []
        (.removeEventListener js/window "keydown" on-key-down))

      :component-did-update
      (fn []
        ;; Add clickaway listener whenever the picker is rendered for the first time
        ;; AFAIK no need to remove it because the element is removed
        (when (and @!el (nil? @!prev-el))
          (.addEventListener js/document "mousedown"
                             #(when-not (parent-of? (.-target %) @!el) (close-picker))))
        (reset! !prev-el @!el))

      :reagent-render
      (fn [{:keys [on-select]}]
        (if (:open @state)
          [:div#emoji-picker {:ref #(reset! !el %)
                                      :style {:position "fixed"
                                              :right (str (+ (-> @state :pos :right) 100) "px")
                                              :top (str (+ (-> @state :pos :top) 0) "px")}}
           [:> (.-Picker emojiMart) {:on-select on-select}]]
          [:button {:class (emoji-picker-button-style)
                    :on-click open-picker}
           [:> (.-Emoji emojiMart) {:emoji "smile" :size 24}]]))})))

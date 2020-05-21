(ns gym.components.loaders
  (:require
   [re-frame.core :refer [subscribe]]
   [cljss.core :refer-macros [defkeyframes defstyles]]))

(defkeyframes ^:private spin []
  {:from {:transform "rotate(0deg)"}
   :to {:transform "rotate(360deg)"}})

(defstyles ^:private circle-style [size]
  {:display "block"
   :height (str size "px")
   :width (str size "px")
   :animation (str (spin) " 3s linear infinite")})

(defstyles ^:private circle-span-style [{:keys [theme size]}]
  {:display "block"
   :position "absolute"
   :top 0
   :bottom 0
   :left 0
   :right 0
   :height (str size "px")
   :width (str size "px")
   :clip (str "rect(" (/ size 2) "px, " size "px, " size "px, 0)")
   :animation (str (spin) " 1.5s cubic-bezier(0.770, 0.000, 0.175, 1.000) infinite")
   :&:before {:content ""
              :display "block"
              :position "absolute"
              :top 0
              :bottom 0
              :left 0
              :right 0
              :margin "auto"
              :height (str size "px")
              :width (str size "px")
              :border "4px solid transparent"
              :border-top (str "4px solid " (:theme-color theme))
              :border-radius "50%"
              :animation (str (spin) " 1.5s cubic-bezier(0.770, 0.000, 0.175, 1.000) infinite")}
   :&:after {:content ""
             :display "block"
             :position "absolute"
             :top 0
             :bottom 0
             :left 0
             :right 0
             :margin "auto"
             :height (str size "px")
             :width (str size "px")
             :border (str "4px solid " (:theme-color-hover theme))
             :border-radius "50%"}})

(defn circle [{:keys [size]}]
  (let [theme @(subscribe [:theme])]
    [:div {:class (circle-style (or size 32))}
     [:span {:class (circle-span-style {:theme theme
                                        :size (or size 32)})}]]))

(ns gym.styles
  (:require
   [clojure.string :refer [join]]
   [cljss.core :refer [inject-global remove-styles!] :refer-macros [defstyles defkeyframes]]))

(def accent-color "hsl(200, 70%, 16%)")
(def accent-color-hover "hsl(200, 70%, 22%)")
(def accent-color-active "hsl(200, 70%, 38%)")

(def main-color "#1d1d1d")
(def main-color-secondary "#2d2d2d")

(def bg-color "#161616")
(def bg-color-secondary "#2d2d2d")

(def text-color "whitesmoke")
(def text-color-secondary "hsl(197, 10%, 45%)")

(def dark-gray "#333333")
(def gray "hsl(197, 10%, 45%)")
(def middle-gray "hsl(210, 6%, 50%)")
(def light-gray "hsl(240, 6%, 97%)")

(def focus-shadow (str "0px 0px 2px " accent-color-active))
(def focus-border (str "solid 1px " accent-color-active))
(def focus-border-inactive "solid 1px transparent")

(def transparent-white "hsla(0, 100%, 100%, 1)")
(def red "rgb(249, 73, 73)")
(def green "hsl(170, 90%, 44%)")
(def blue "rgb(0, 53, 167)")

(def font-family "-apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Oxygen, Ubuntu, Cantarell, \"Fira Sans\", \"Droid Sans\", \"Helvetica Neue\", sans-serif")

(defkeyframes shimmer [from to]
  {:from {:transform (str "background-position: " from " 0")}
   :to {:transform (str "background-position: " to " 0")}})

(defn inject-global-styles []
  (remove-styles!)
  (inject-global {"body, html" {:margin 0
                                :padding 0
                                :height "100%"
                                :width "100%"
                                :background-color bg-color
                                :font-family font-family
                                :-webkit-font-smoothing "antialiased"
                                :-moz-osx-font-smoothing "grayscale"}
                  :body {:color gray
                         :overflow-x "hidden"}
                  :button {:border "none"
                           :margin 0
                           :padding 0
                           :background "none"
                           :color "inherit"
                           :font "inherit"
                           :cursor "pointer"
                           :-webkit-appearance "none"
                           :-moz-appearance "none"}
                  "h1, h2" {:margin 0
                            :font "inherit"}}))

(defn classes [& classnames]
  (join " " classnames))

(defstyles icon-button []
  {:margin 0
   :background "none"
   :cursor "pointer"
   :font-size "125%"
   :display "flex"
   :align-items "center"
   :color gray
   :border-radius "6px"
   :border "solid 1px transparent"
   :padding "0.5rem"
   :&:hover {:background accent-color
             :color text-color}
   :&:focus {:outline "none"
             :border focus-border
             :box-shadow focus-shadow}
   :&:active {:color main-color
              :background middle-gray}
   "> * + *" {:margin-left "0.25rem"}
   "&:focus:not(:focus-visible)" {:box-shadow "none !important"
                                  :border (str focus-border-inactive " !important")}})

(defstyles icon-button-cta []
  {:margin 0
   :cursor "pointer"
   :font-size "125%"
   :display "flex"
   :align-items "center"
   :border-radius "6px"
   :border "solid 1px transparent"
   :background-color accent-color
   :color text-color
   :padding "0.5rem 1rem"
   :&:hover {:background-color accent-color-hover}
   :&:active {:background-color accent-color-active}
   :&:focus {:border-color text-color
             :box-shadow (str "0px 0px 4px 1px " main-color)}})

(defstyles base-icon []
  {:color text-color})

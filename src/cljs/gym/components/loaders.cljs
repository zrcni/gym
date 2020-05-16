(ns gym.components.loaders
  (:require
   [gym.styles :as styles]
   [cljss.reagent :refer-macros [defstyled]]
   [cljss.core :refer-macros [defkeyframes]]))

;; @keyframes load8 {
;;   0% {
;;     -webkit-transform: rotate(0deg);
;;     transform: rotate(0deg);
;;   }
;;   100% {
;;     -webkit-transform: rotate(360deg);
;;     transform: rotate(360deg);
;;   }
;; }

(defkeyframes spin [from to]
  {:from {:transform (str "rotate(" from "reg)")}
   :to {:transform (str "rotate(" to "reg)")}})

(defstyled circle :div
  {:margin "60px auto"
   :font-size "10px"
   :position "relative"
   :text-indent "-9999em"
   :border-top (str "1.1em solid " styles/main-color)
   :border-right (str "1.1em solid " styles/main-color)
   :border-bottom (str "1.1em solid " styles/main-color)
   :border-left "1.1em solid #ffffff"
   :-webkit-transform "translateZ(0)"
   :-ms-transform "translateZ(0)"
   :transform "translateZ(0)"
   :-webkit-animation (str (spin 0 360) " 1.1s infinite linear")
   :animation (str (spin 0 360) " 1.1s infinite linear")
   :border-radius "50%"
   :width "10em"
   :height "10em"
   :&:after {:border-radius "50%"
             :width "10em"
             :height "10em"}})
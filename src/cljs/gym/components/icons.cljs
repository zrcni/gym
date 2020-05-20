(ns gym.components.icons)

(defn make-fas-icon [name]
  (fn [{:keys [class]} & props]
    [:i {:class (str "fas fa-" name (when class (str " " class)))} props]))

(def chevron-up (make-fas-icon "chevron-up"))
(def chevron-down (make-fas-icon "chevron-down"))
(def plus (make-fas-icon "plus"))
(def minus (make-fas-icon "minus"))
(def trash (make-fas-icon "trash"))
(def plus-circle (make-fas-icon "plus-circle"))
(def times (make-fas-icon "times"))
(def power-off (make-fas-icon "power-off"))
(def bars (make-fas-icon "bars"))
(def cog (make-fas-icon "cog"))

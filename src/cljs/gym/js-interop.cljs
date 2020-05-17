(ns gym.js-interop)

(defn get-value
  "Gets the event target value"
  [^js/Event e]
  (-> e .-target .-value))

(defn str->num
  "Converts a string into a number
   Returns nil instead of NaN when the string can't be converted."
  [str]
  (let [n (js/parseInt str 10)]
    (if (js/isNaN n)
      nil
      n)))

(ns gym.frontend.dom-utils)

(defn parent-of? [el parent]
  (if-not (.-parentNode el)
    false
    (if (= (.-parentNode el) parent)
      true
      (parent-of? (.-parentNode el) parent))))

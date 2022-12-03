(ns gym.frontend.calendar.tags)

(def ^:private max-suggested-tags 3)

(defn consolidate-suggested-tags [prev curr]
  (take max-suggested-tags (concat curr prev)))

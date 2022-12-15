(ns gym.frontend.calendar.tags)

(def ^:private max-count 3)

(defn consolidate-suggested-tags [prev curr]
  (->> (concat curr prev)
       (distinct)
       (take max-count)))

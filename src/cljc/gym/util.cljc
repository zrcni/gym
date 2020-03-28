(ns gym.util)

(defn includes? [coll el]
  (if (some #{el} coll) true false))

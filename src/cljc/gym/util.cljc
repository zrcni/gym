(ns gym.util)

(defn includes? [coll el]
  (if (some #{el} coll) true false))

(defn inc-by [n]
  (fn [m]
    (if (nil? m) n (+ n m))))

(defn dec-by [n]
  (fn [m]
    (if (nil? m) 0 (- m n))))

(defn contains-many? [m ks]
  (every? #(contains? m %) ks))

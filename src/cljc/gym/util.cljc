(ns gym.util
  #?(:clj (:import java.util.UUID))
   (:require
    [clojure.spec.alpha :as s]))

(defn exception [message]
  #?(:clj (Exception. message)
     :cljs (Error. message)))

(defn create-uuid [s]
  #?(:clj (UUID/fromString s)
     :cljs (uuid s)))

(defn generate-uuid []
  #?(:clj (UUID/randomUUID)
     :cljs (random-uuid)))

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


(def ^:private problems-kw
  #?(:clj :clojure.spec.alpha/problems
     :cljs :cljs.spec.alpha/problems))

(defn ^:private resolve-invalid-keys [explain-data-result]
  (when-let [invalid-keys (get explain-data-result problems-kw)]
    (mapcat #(as-> % v
               (:in v)
               (map name v))
            invalid-keys)))

(defn make-validate-fn
  "Returns a function that validates data with the specified spec (keyword)"
  [spec-keyword]
  (fn [data]
    (when-let [result (s/explain-data spec-keyword data)]
      (resolve-invalid-keys result))))

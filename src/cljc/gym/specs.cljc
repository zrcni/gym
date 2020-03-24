(ns gym.specs
  (:require 
   [clojure.string :as string]
   [clojure.spec.alpha :as s]))

;; TODO: finish this function,
;;       find a complete predicate
;;       or create a new predicate utilizing reader conditionals
;; (defn local-date? [s]
;;   (let [parts (str/split s "-")]
;;     (if (= 3 (count parts))
;;       (let [[y m d] parts]
;;         (if (and
;;              (= 4 (count y))
;;              (= 2 (count m))
;;              (= 2 (count d)))
;;           true
;;           false))
;;       false)))

(defn local-date? [s]
  (and (string? s) (= 10 (count s))))

(def max-description-length 200)

(s/def :gym.specs/description
  (s/and string?
         #(not (string/blank? %))
         #(< (count %) max-description-length)))

(s/def :gym.specs/workout_id
  (s/and string? #(uuid? %)))

(s/def :gym.specs/date local-date?)

(s/def :gym.specs/duration
  (s/and integer? #(>= % 0)))

;; TODO: create a predicate function for date
(s/def :gym.specs/created_at (s/and string? #(> (count %) 10)))
(s/def :gym.specs/modified_at (s/and string? #(> (count %) 10)))

(s/def :gym.specs/workout (s/keys :req-un [:gym.specs/workout_id
                                           :gym.specs/description
                                           :gym.specs/date
                                           :gym.specs/duration]
                                  :opt-un [:gym.specs/created_at
                                        :gym.specs/modified_at]))

;; this is for validating a new workout to be created
(s/def :gym.specs/workout-new
  (s/keys :req-un [:gym.specs/description
                   :gym.specs/date
                   :gym.specs/duration]))

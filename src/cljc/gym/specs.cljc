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

(s/def ::description
  (s/and string?
         #(not (string/blank? %))
         #(< (count %) max-description-length)))

(s/def ::workout_id
  (s/and string? #(uuid? %)))

(s/def ::date local-date?)

(s/def ::duration
  (s/and integer? #(>= % 0)))

;; TODO: validate dates properly
(s/def ::created_at (s/and string? #(> (count %) 10)))
(s/def ::modified_at (s/and string? #(> (count %) 10)))
;; TODO: validate time of day (HH:mm) properly
(s/def ::start_time (s/and string? #(= (count %) 5)))

(s/def ::workout (s/keys :req-un [::workout_id
                                  ::description
                                  ::date
                                  ::duration]
                         :opt-un [::created_at
                                  ::modified_at
                                  ::start_time]))

;; this is for validating a new workout to be created
(s/def ::workout-new
  (s/keys :req-un [::description
                   ::date
                   ::duration]
          :opt-un [::start_time]))

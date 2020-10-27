(ns gym.workout
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
  (s/and string? uuid?))

(s/def ::user_id
  (s/and string? uuid?))

(s/def ::date local-date?)

(s/def ::duration
  (s/and integer? #(>= % 0)))

;; TODO: validate dates properly
(s/def ::created_at (s/and string? #(> (count %) 10)))
(s/def ::modified_at (s/and string? #(> (count %) 10)))
;; TODO: validate time of day (HH:mm) properly
(s/def ::start_time (s/and string? #(= (count %) 5)))

(def min-tag-length 2)
(def max-tag-length 20)

(defn tag? [tag]
  (and
   (string? tag)
   #(>= (count tag) min-tag-length)
   #(<= (count tag) max-tag-length)))

(s/def ::tag tag?)
(s/def ::tags (s/coll-of tag? :kind vector?))

(s/def ::workout
  (s/keys :req-un [::workout_id
                   ::user_id
                   ::description
                   ::date
                   ::duration
                   ::tags]
          :opt-un [::created_at
                   ::modified_at
                   ::start_time]))

;; this is for validating a new workout to be created
(s/def ::workout-new
  (s/keys :req-un [::description
                   ::date
                   ::duration
                   ::tags]
          :opt-un [::start_time]))

(def ^:private problems-kw
  #?(:clj :clojure.spec.alpha/problems
     :cljs :cljs.spec.alpha/problems))

(defn ^:private resolve-invalid-keys [explain-data-result]
  (when-let [invalid-keys (get explain-data-result problems-kw)]
    (mapcat #(as-> % v
               (:in v)
               (map name v))
            invalid-keys)))

(defn ^:private make-validate-fn
  "Returns a function that validates data with the specified spec (keyword)"
  [spec-keyword]
  (fn [data]
    (when-let [result (s/explain-data spec-keyword data)]
      (resolve-invalid-keys result))))

(def validate-workout (make-validate-fn ::workout))
(def validate-workout-new (make-validate-fn ::workout-new))

;; Created workout
(defrecord Workout [workout_id
                    user_id
                    description
                    date
                    duration
                    created_at
                    modified_at
                    tags])

(defn make-workout [workout]
  (apply ->Workout (map workout [:workout_id
                                 :user_id
                                 :description
                                 :date
                                 :duration
                                 :created_at
                                 :modified_at
                                 :tags])))

;; Workout which has not been created yet
(defrecord WorkoutNew [description
                       date
                       duration
                       tags])

(defn make-workout-new [workout]
  (apply ->WorkoutNew (map workout [:description
                                    :date
                                    :duration
                                    :tags])))

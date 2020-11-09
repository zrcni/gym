(ns gym.workouts.domain
  (:require
   [clojure.string :as string]
   [clojure.spec.alpha :as s]
   [gym.util :refer [make-validate-fn generate-uuid exception]]))

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
                   ::tags
                   ::created_at
                   ::modified_at]
          :opt-un [::start_time]))

(def validate-workout (make-validate-fn ::workout))

(defrecord Workout [workout_id user_id description
                    date duration tags
                    created_at modified_at])

(defn create
  ([{:keys [workout_id user_id description date duration tags created_at modified_at]}]
   (let [workout  (->Workout workout_id user_id description
                             date duration tags
                             created_at modified_at)]
     (when-let [invalid-keys (validate-workout workout)]
       (throw (exception (str "Invalid workout properties: " (first invalid-keys)))))
     workout))

  ([user_id description date duration tags]
   (create {:workout_id (generate-uuid)
            :user_id user_id
            :description description
            :date date
            :duration duration
            :tags tags})))

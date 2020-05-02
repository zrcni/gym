(ns gym.workouts
  (:import java.time.LocalDate
           java.util.UUID)
  (:require
   [gym.database :refer [get-db]]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]))

(defn local-date->string
  "Transforms LocalDate class into a string of YYYY-MM-DD"
  [date]
  (.toString date))

(defn workout-from-db [row]
  (-> row
      (update :date local-date->string)))

;; gets the tag string from the tag object/row
(defn tag-from-db [row]
  (:tag row))

;; TODO: get tags
(defn get-by-user-id [user-id]
  (let [workouts (sql/query (get-db)
                            ["SELECT * FROM workouts WHERE user_id = ?" user-id]
                            {:builder-fn rs/as-unqualified-maps})]
    (map workout-from-db workouts)))

;; TODO: get tags
(defn get-by-id [workout-id]
  (let [workout (sql/get-by-id (get-db)
                               "workouts"
                               (UUID/fromString workout-id)
                               "workout_id"
                               {:builder-fn rs/as-unqualified-maps})]
    (when workout (workout-from-db workout))))

;; TODO: use a transaction for inserting workouts & tags
(defn create! [{:keys [description duration date tags user_id]}]
  (let [workout (sql/insert! (get-db)
                             "workouts"
                             {:description description
                              :duration duration
                              :date (LocalDate/parse date)
                              :user_id user_id}
                             {:return-keys true
                              :builder-fn rs/as-unqualified-maps})
        asd (println "?????: " (map #([(:workout_id workout) %]) tags))
        tags (sql/insert-multi! (get-db)
                                "workouts"
                                [:workout_id :tag]
                                (map #([(:workout_id workout) %]) tags)
                                {:return-keys true
                                 :builder-fn rs/as-unqualified-maps})]
    (prn "TAGS: " tags)
    (when workout
      (-> (workout-from-db workout)
          (assoc :tags (map #(tag-from-db %) tags))))))

;; TODO: remove tags
(defn delete-by-id!
  "returns delete count"
  [workout-id]
  (let [result (sql/delete! (get-db)
                            "workouts"
                            ["workout_id = ?" (UUID/fromString workout-id)])]
    (:next.jdbc/update-count result)))

;; (create! {:description "SADSASD" :duration (* 120 1000) :date "2020-03-21"})

;; (prn (get-by-id "5f392a97-dafc-4b2a-b739-a46ba677cfcd"))

;; (prn (delete-by-id! "5f392a97-dafc-4b2a-b739-a46ba677cfcd"))

;; (let [res (create-workout {:description "just worked out *cough*" :date "2020-03-20" :duration (* 30 (* 60 1000))})]
;;   (prn (str "Result: " res)))

;; (try
;;   (dostuff)
;;   catch Exception e (prn (.getMessage e)))

;; (defn validate-workout [workout]
;;   (case ))

;; (defn validate-description [description]
;;   (case
;;    (nil? description) "Description is required"
;;     (or (nil? description) (= 0 (count (s/trim description)))) "Description must not be empty"
;;     :else nil))

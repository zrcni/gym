(ns gym.workouts.repository
  (:import java.time.LocalDate
           java.util.UUID)
  (:require
   [gym.database :refer [get-db]]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]))

(defn workouts-with-tags-query [& [where limit]]
  (let [where-clause (if where (str " WHERE " where " ") "")
        limit-clause (if limit (str " LIMIT " limit) "")]
    (str "SELECT workouts.workout_id, workouts.user_id, workouts.description, workouts.duration, workouts.date, workouts.created_at, workouts.modified_at, ARRAY_AGG (workout_tags.tag) tags"
         " FROM workouts"
         " INNER JOIN workout_tags"
         " ON workouts.workout_id = workout_tags.workout_id"
         where-clause
         " GROUP BY workouts.workout_id"
         limit-clause)))

(defn local-date->string
  "Transforms LocalDate class into a string of YYYY-MM-DD"
  [date]
  (.toString date))

(defn row->workout [row]
 (-> row
     (update :date local-date->string)))

(defn row->workout-and-tags [row]
 (as-> row r
   (update r :date local-date->string)
   (assoc r :tags (vec (.getArray (:tags r))))))

;; gets the tag string from the tag object/row
(defn row->tag [row]
 (:tag row))

(defn get-by-user-id [user-id]
  (let [workouts (sql/query (get-db)
                            [(workouts-with-tags-query "user_id = ?") (UUID/fromString user-id)]
                            {:builder-fn rs/as-unqualified-maps})]
    (map row->workout-and-tags workouts)))

(defn get-by-id [workout-id]
  (let [workouts (sql/query (get-db)
                            [(workouts-with-tags-query "workout_id = ?" 1) (UUID/fromString workout-id)]
                            {:builder-fn rs/as-unqualified-maps})]
    (if (> (count workouts) 0)
      (row->workout-and-tags (first workouts))
      nil)))

(defn create! [{:keys [description duration date tags user_id]}]
  (jdbc/with-transaction [tx (get-db)]
    (let [workout (sql/insert! tx
                               "workouts"
                               {:description description
                                :duration duration
                                :date (LocalDate/parse date)
                                :user_id (UUID/fromString user_id)}
                               {:return-keys true
                                :builder-fn rs/as-unqualified-maps})
          tags (sql/insert-multi! tx
                                  "workout_tags"
                                  [:workout_id :tag]
                                  (vec (map #(vector (:workout_id workout) %) tags))
                                  {:return-keys true
                                   :builder-fn rs/as-unqualified-maps})]
      (-> (row->workout workout)
          (assoc :tags (map #(row->tag %) tags))))))

(defn delete-by-id!
  "returns delete count"
  [workout-id]
  (jdbc/with-transaction [tx (get-db)]
    (let [w-id (UUID/fromString workout-id)
          _ (sql/delete! tx
                         "workout_tags"
                         ["workout_id = ?" w-id])
          result (sql/delete! tx
                              "workouts"
                              ["workout_id = ?" w-id])]
      (:next.jdbc/update-count result))))

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

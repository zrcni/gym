(ns gym.workouts.repository.postgresql-workout-repository
  (:import java.time.LocalDate)
  (:require [gym.util :refer [create-uuid]]
            [gym.events.domain-events :refer [dispatch-event]]
            [gym.workouts.events :refer [workout-created workout-deleted]]
            [gym.workouts.repository.workout-repository :refer [WorkoutRepository get-workout-by-workout-id]]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(defn workouts-with-tags-query [& [where limit]]
  (let [where-clause (if where (str " WHERE " where) "")
        limit-clause (if limit (str " LIMIT " limit) "")]
    (str "SELECT"
         " workouts.workout_id, workouts.user_id, workouts.description,"
         " workouts.duration, workouts.date, workouts.created_at, workouts.modified_at,"
         " coalesce(array_agg(workout_tags.tag) filter (where workout_tags.tag is not null), '{}') AS tags"
         " FROM workouts"
         " LEFT JOIN workout_tags"
         " ON workouts.workout_id = workout_tags.workout_id"
         where-clause
         " GROUP BY workouts.workout_id"
         limit-clause)))

(defn local-date->string
  "Transforms LocalDate class into a string of YYYY-MM-DD"
  [date]
  (str date))

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

(defn format-duration-result [result]
  (assoc result :user_id (str (:user_id result))))



(defrecord PostgresqlWorkoutRepository [db-conn domain-events]
  WorkoutRepository

  (get-workouts-by-user-id
    [this user_id]
    (let [workouts (sql/query db-conn
                              [(workouts-with-tags-query "workouts.user_id = ?") (create-uuid user_id)]
                              {:builder-fn rs/as-unqualified-maps})]
      (map row->workout-and-tags workouts)))

  (get-workout-by-workout-id
    [this workout_id]
    (let [workouts (sql/query db-conn
                              [(workouts-with-tags-query "workouts.workout_id = ?" 1) (create-uuid workout_id)]
                              {:builder-fn rs/as-unqualified-maps})]
      (if (> (count workouts) 0)
        (row->workout-and-tags (first workouts))
        nil)))

  (create-workout!
    [this {:keys [description duration date tags user_id]}]
    (jdbc/with-transaction [tx db-conn]
      (let [workout (sql/insert! tx
                                 "workouts"
                                 {:description description
                                  :duration duration
                                  :date (LocalDate/parse date)
                                  :user_id (create-uuid user_id)}
                                 {:return-keys true
                                  :builder-fn rs/as-unqualified-maps})
            tags (sql/insert-multi! tx
                                    "workout_tags"
                                    [:workout_id :tag]
                                    (vec (map #(vector (:workout_id workout) %) tags))
                                    {:return-keys true
                                     :builder-fn rs/as-unqualified-maps})
            workout (-> (row->workout workout)
                        (assoc :tags (map #(row->tag %) tags)))]

        (dispatch-event domain-events (workout-created workout))

        workout)))

  (delete-workout-by-workout-id!
   [this workout_id]
   (let [workout (get-workout-by-workout-id this workout_id)]

     (jdbc/with-transaction [tx db-conn]
       (let [w-id (create-uuid workout_id)
             _ (sql/delete! tx
                            "workout_tags"
                            ["workout_id = ?" w-id])
             deleted-workout (jdbc/execute-one! tx
                                                ["DELETE FROM workouts WHERE workout_id = ? RETURNING workout_id, user_id, duration, date" w-id]
                                                {:builder-fn rs/as-unqualified-maps})]
         (if deleted-workout
           (do
             (dispatch-event domain-events (workout-deleted workout))
             1)
           0)))))

  (get-all-workout-durations
   [this start-date end-date]
   (let [results (sql/query db-conn
                            ["SELECT SUM(duration), user_id FROM workouts WHERE date BETWEEN SYMMETRIC ? AND ? GROUP BY user_id" start-date end-date]
                            {:builder-fn rs/as-unqualified-maps})]
     (map format-duration-result results))))



(defn create-postgresql-workout-repository [db-conn domain-events]
  (->PostgresqlWorkoutRepository db-conn domain-events))

(ns gym.backend.workouts.repository.postgresql-workout-repository
  (:import java.time.LocalDate)
  (:require [gym.util :refer [create-uuid]]
            [gym.backend.workouts.repository.workout-repository :refer [WorkoutRepository]]
            [next.jdbc.sql :as jdbc-sql]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

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
    (update r :date local-date->string)))

;; gets the tag string from the tag object/row
(defn row->tag [row]
  (:tag row))



(defrecord PostgresqlWorkoutRepository [db-conn]
  WorkoutRepository

  (get-workouts-by-user-id
    [_ user_id]
    (let [workouts (jdbc-sql/query db-conn
                              [(workouts-with-tags-query "workouts.user_id = ?") (create-uuid user_id)]
                              {:builder-fn rs/as-unqualified-maps})]
      (map row->workout-and-tags workouts)))

  (get-workout-by-workout-id
    [_ workout_id]
    (let [workouts (jdbc-sql/query db-conn
                              [(workouts-with-tags-query "workouts.workout_id = ?" 1) (create-uuid workout_id)]
                              {:builder-fn rs/as-unqualified-maps})]
      (if (> (count workouts) 0)
        (row->workout-and-tags (first workouts))
        nil)))

  (create-workout!
    [_ {:keys [description duration date tags user_id]}]
    (jdbc/with-transaction [tx db-conn]
      (let [workout (jdbc-sql/insert! tx
                                 "workouts"
                                 {:description description
                                  :duration duration
                                  :date (LocalDate/parse date)
                                  :user_id (create-uuid user_id)}
                                 {:return-keys true
                                  :builder-fn rs/as-unqualified-maps})
            tags (jdbc-sql/insert-multi! tx
                                         "workout_tags"
                                         [:workout_id :tag]
                                         (vec (map #(vector (:workout_id workout) %) tags))
                                         {:return-keys true
                                          :builder-fn rs/as-unqualified-maps})]

        (-> (row->workout workout)
            (assoc :tags (map #(row->tag %) tags))))))

  (delete-workout-by-workout-id!
   [_ workout_id]
   (jdbc/with-transaction [tx db-conn]
     (let [w-id (create-uuid workout_id)
           _ (jdbc-sql/delete! tx
                               "workout_tags"
                               ["workout_id = ?" w-id])
           deleted-workout (jdbc/execute-one! tx
                                              ["DELETE FROM workouts WHERE workout_id = ? RETURNING workout_id, user_id, duration, date" w-id]
                                              {:builder-fn rs/as-unqualified-maps})]
       (if deleted-workout
         1
         0)))))



(defn create-postgresql-workout-repository [db-conn]
  (->PostgresqlWorkoutRepository db-conn))

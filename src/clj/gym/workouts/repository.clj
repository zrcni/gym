(ns gym.workouts.repository
  (:import java.time.LocalDate
           java.time.temporal.TemporalAdjusters
           java.time.DayOfWeek
           java.util.UUID)
  (:require
   [gym.stats.counter :refer [current-week-exercise-durations
                              current-month-exercise-durations]]
   [gym.database :refer [get-db]]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]))

(defn current-week? [local-date]
  (let [today (LocalDate/now)
        start-of-week (.with today DayOfWeek/MONDAY)
        end-of-week (.with today DayOfWeek/SUNDAY)]
    (and (.isAfter local-date start-of-week)
         (.isBefore local-date end-of-week))))

(defn current-month? [local-date]
  (let [today (LocalDate/now)
        start-of-month (.with today (TemporalAdjusters/firstDayOfMonth))
        end-of-month (.with today (TemporalAdjusters/lastDayOfMonth))]
    (and (.isAfter local-date start-of-month)
         (.isBefore local-date end-of-month))))

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

(defn get-by-user-id [user_id]
  (let [workouts (sql/query (get-db)
                            [(workouts-with-tags-query "workouts.user_id = ?") (UUID/fromString user_id)]
                            {:builder-fn rs/as-unqualified-maps})]
    (map row->workout-and-tags workouts)))

(defn get-by-id [workout_id]
  (let [workouts (sql/query (get-db)
                            [(workouts-with-tags-query "workouts.workout_id = ?" 1) (UUID/fromString workout_id)]
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
                                   :builder-fn rs/as-unqualified-maps})
          duration-sec (/ duration 1000)]
      (when (current-week? (LocalDate/parse date))
        ((-> current-week-exercise-durations :inc) user_id duration-sec))
      (when (current-month? (LocalDate/parse date))
        ((-> current-month-exercise-durations :inc) user_id duration-sec))

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
          workout (jdbc/execute-one! tx
                                     ["DELETE FROM workouts WHERE workout_id = ? RETURNING workout_id, user_id, duration, date" w-id]
                                     {:builder-fn rs/as-unqualified-maps})
          duration-sec (/ (:duration workout) 1000)
          local-date (.toLocalDate (:date workout))]
      (if workout
        (do
          (when (current-week? local-date)
            ((-> current-week-exercise-durations :dec) (-> workout :user_id .toString) duration-sec))
          (when (current-month? local-date)
            ((-> current-month-exercise-durations :dec) (-> workout :user_id .toString) duration-sec))
          1)
        0))))

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

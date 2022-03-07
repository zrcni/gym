(ns gym.backend.analytics.workouts-by-day-of-week
  (:require [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [gym.util :refer [create-uuid]]))

(def query
  "SELECT CAST(extract(isodow from \"public\".\"workouts\".\"date\") AS integer) AS \"date\", \"tags\".\"tag\" AS \"tag\", count(*) AS \"count\"
   FROM \"public\".\"workouts\"
   LEFT JOIN \"public\".\"workout_tags\" tags ON \"public\".\"workouts\".\"workout_id\" = \"tags\".\"workout_id\"
   WHERE \"public\".\"workouts\".\"user_id\" = ?
   GROUP BY CAST(extract(isodow from \"public\".\"workouts\".\"date\") AS integer), \"tags\".\"tag\"
   ORDER BY CAST(extract(isodow from \"public\".\"workouts\".\"date\") AS integer) ASC, \"tags\".\"tag\" ASC")

(defn controller [req]
  (let [user-id (create-uuid (-> req :user :user_id))
        res (sql/query (-> req :deps :postgres)
                       [query user-id]
                       {:builder-fn rs/as-unqualified-maps})]
    {:status 200
     :body res}))

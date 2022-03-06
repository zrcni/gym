(ns gym.backend.analytics.controllers.workout-duration-by-tag
  (:require [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [gym.util :refer [create-uuid]]))

(def query
  "SELECT \"tags\".\"tag\" AS \"tag\", sum(\"public\".\"workouts\".\"duration\") AS \"duration\"
   FROM \"public\".\"workouts\"
   LEFT JOIN \"public\".\"workout_tags\" \"tags\" ON \"public\".\"workouts\".\"workout_id\" = \"tags\".\"workout_id\"
   WHERE \"public\".\"workouts\".\"user_id\" = ?
   GROUP BY \"tags\".\"tag\"
   ORDER BY \"tags\".\"tag\" ASC")

(defn create [postgres]
  (fn [req]
    (let [user-id (create-uuid (-> req :context :user :user_id))
          res (sql/query postgres
                         [query user-id]
                         {:builder-fn rs/as-unqualified-maps})]
      {:status 200
       :body res})))

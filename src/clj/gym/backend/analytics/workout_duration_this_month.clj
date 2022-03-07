(ns gym.backend.analytics.workout-duration-this-month
  (:require [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [gym.util :refer [create-uuid]]))

(def query
  "SELECT sum(\"public\".\"workouts\".\"duration\") AS \"sum\"
   FROM \"public\".\"workouts\"
   WHERE (\"public\".\"workouts\".\"user_id\" = ?
   AND \"public\".\"workouts\".\"date\" >= date_trunc('month', now()))")

(defn controller [req]
  (let [user-id (create-uuid (-> req :user :user_id))
        res (sql/query (-> req :deps :postgres)
                       [query user-id]
                       {:builder-fn rs/as-unqualified-maps})]
    {:status 200
     :body {:duration (or (-> res first :sum) 0)}}))

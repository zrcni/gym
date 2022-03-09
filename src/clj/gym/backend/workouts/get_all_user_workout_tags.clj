(ns gym.backend.workouts.get-all-user-workout-tags
  (:require [gym.util :refer [create-uuid]]
            [next.jdbc.sql :as jdbc-sql]
            [honey.sql.helpers :as h]
            [honey.sql :as sql]
            [next.jdbc.result-set :as rs]))

(defn query [user-id]
  (-> (h/select-distinct :public.workout_tags.tag)
      (h/from :public.workout_tags)
      (h/left-join [:public.workouts :workouts] [:= :public.workout_tags.workout_id :workouts.workout_id])
      (h/where [:= :workouts.user_id user-id])
      (sql/format)))

(defn controller [req]
  (let [postgres (-> req :deps :postgres)
        user-id (get-in req [:user :user_id])
        tags  (->> (jdbc-sql/query postgres
                                   (query (create-uuid user-id))
                                   {:builder-fn rs/as-unqualified-maps})
                   (map :tag))]

    {:status 200
     :body tags}))

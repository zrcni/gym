(ns gym.backend.analytics.analytics-query
  (:require [next.jdbc.sql :as jdbc-sql]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [gym.util :refer [create-uuid]]))

;; List of values to search params are provided
;; as individual key-value pairs e.g. ?tags=walk&tags=ski
(defn search-param->vector [value]
  (if (vector? value)
    value
    (if value
      [value]
      nil)))

(def workout-duration-by-tag
  {:query (fn [{:keys [user-id exclude]}]
            (let [exclude (search-param->vector exclude)]
              (-> (h/select [:tags.tag #_as :tag] [[:sum :public.workouts.duration] #_as :duration])
                  (h/from :public.workouts)
                  (h/left-join [:public.workout_tags :tags] [:= :public.workouts.workout_id :tags.workout_id])
                  (h/where (let [where [:= :public.workouts.user_id user-id]]
                             (if (empty? exclude)
                               where
                               [:and where
                                [:not-in :tag exclude]])))
                  (h/group-by :tags.tag)
                  (h/order-by [:tags.tag :asc])
                  (sql/format))))
   :params {:req [:user-id]
            :opt [:exclude]}})

(def workout-duration-this-week
  {:query "SELECT sum(\"public\".\"workouts\".\"duration\") AS \"sum\"
           FROM \"public\".\"workouts\"
           WHERE (\"public\".\"workouts\".\"user_id\" = ?
           AND \"public\".\"workouts\".\"date\" >= date_trunc('week', now()))"
   :params {:req [:user-id]}
   :resolve
   (fn [res]
     {:duration (or (-> res first :sum) 0)})})

(def workout-duration-this-month
  {:query "SELECT sum(\"public\".\"workouts\".\"duration\") AS \"sum\"
           FROM \"public\".\"workouts\"
           WHERE (\"public\".\"workouts\".\"user_id\" = ?
           AND \"public\".\"workouts\".\"date\" >= date_trunc('month', now()))"
   :params {:req [:user-id]}
   :resolve
   (fn [res]
     {:duration (or (-> res first :sum) 0)})})

(def workouts-by-day-of-week
  {:query "SELECT CAST(extract(isodow from \"public\".\"workouts\".\"date\") AS integer) AS \"date\", \"tags\".\"tag\" AS \"tag\", count(*) AS \"count\"
           FROM \"public\".\"workouts\"
           LEFT JOIN \"public\".\"workout_tags\" tags ON \"public\".\"workouts\".\"workout_id\" = \"tags\".\"workout_id\"
           WHERE \"public\".\"workouts\".\"user_id\" = ?
           GROUP BY CAST(extract(isodow from \"public\".\"workouts\".\"date\") AS integer), \"tags\".\"tag\"
           ORDER BY CAST(extract(isodow from \"public\".\"workouts\".\"date\") AS integer) ASC, \"tags\".\"tag\" ASC"
   :params {:req [:user-id]}
   :resolve
   (fn [res]
     {:all-tags (reduce #(conj %1 (:tag %2)) #{} res)
      :entries (->> res
                    (group-by :date)
                    (map (fn [[date maps]]
                           (reduce (fn [acc {:keys [tag count]}]
                                     (assoc acc tag count))
                                   {:date date}
                                   maps))))})})

(def workouts-by-month-of-year
  {:query "SELECT CAST(extract(month from \"public\".\"workouts\".\"date\") AS integer) AS \"date\", \"tags\".\"tag\" AS \"tag\", count(*) AS \"count\"
           FROM \"public\".\"workouts\"
           LEFT JOIN \"public\".\"workout_tags\" \"tags\" ON \"public\".\"workouts\".\"workout_id\" = \"tags\".\"workout_id\"
           WHERE \"public\".\"workouts\".\"user_id\" = ?
           GROUP BY CAST(extract(month from \"public\".\"workouts\".\"date\") AS integer), \"tags\".\"tag\"
           ORDER BY CAST(extract(month from \"public\".\"workouts\".\"date\") AS integer) ASC, \"tags\".\"tag\" ASC"
   :params {:req [:user-id]}
   :resolve
   (fn [res]
     {:all-tags (reduce #(conj %1 (:tag %2)) #{} res)
      :entries (->> res
                    (group-by :date)
                    (map (fn [[date maps]]
                           (reduce (fn [acc {:keys [tag count]}]
                                     (assoc acc tag count))
                                   {:date date}
                                   maps)))
                    (sort-by :date))})})

(def queries
  {:workout-duration-this-week workout-duration-this-week
   :workout-duration-this-month workout-duration-this-month
   :workout-duration-by-tag workout-duration-by-tag
   :workouts-by-day-of-week workouts-by-day-of-week
   :workouts-by-month-of-year workouts-by-month-of-year})

(defn validate-input-params [query input-params]
  (let [required-params (-> query queries :params :req)
        missing-param-indexes (keep-indexed #(when (and (nil? %2)
                                                        (contains? required-params %1))
                                               %1)
                                            input-params)]
    (cond
      (empty? missing-param-indexes)
      {:error "Query parameters are missing"
       :missing-params (map #(get input-params %) missing-param-indexes)}

      :else
      nil)))


(defn controller [req]
  (if-let [{:keys [query params resolve] :or {resolve identity}} (queries (-> req :params :query keyword))]
    (let [user-id (create-uuid (-> req :user :user_id))
          req-params (-> (:params req)
                         (dissoc :query)
                         (assoc :user-id user-id))
          input-params (map #(% req-params) (-> params vals flatten))
          error (validate-input-params query input-params)]

      ;; All query params are required
      (if-not error
        {:status 400
         :body error}

        {:status 200
         :body (resolve (jdbc-sql/query (-> req :deps :postgres)
                                        (if (fn? query)
                                          (query req-params)
                                          (cons query input-params))
                                        {:builder-fn rs/as-unqualified-maps}))}))

    {:status 404
     :body {:error "Query does not exist"}}))

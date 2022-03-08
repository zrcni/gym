(ns gym.backend.analytics.analytics-query
  (:require [next.jdbc.sql :as jdbc-sql]
            [next.jdbc.result-set :as rs]
            [gym.util :refer [create-uuid]]
            [gym.backend.analytics.query-defs :refer [query-defs]]))

(defn validate-input-params [params  input-params]
  (let [required-params (:req params)
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
  (if-let [{:keys [build params resolve] :or {resolve identity}} (query-defs (-> req :params :query keyword))]
    (let [user-id (create-uuid (-> req :user :user_id))
          req-params (-> (:params req)
                         (dissoc :query)
                         (assoc :user-id user-id))
          input-params (map #(% req-params) (-> params vals flatten))
          error (validate-input-params params input-params)]

      ;; All query params are required
      (if-not error
        {:status 400
         :body error}

        {:status 200
         :body  (-> (jdbc-sql/query (-> req :deps :postgres)
                                    (build req-params)
                                    {:builder-fn rs/as-unqualified-maps})
                    (resolve))}))

    {:status 404
     :body {:error "Query does not exist"}}))

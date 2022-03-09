(ns gym.backend.system.postgres
  (:import java.sql.Array)
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(extend-protocol rs/ReadableColumn
  Array
  (read-column-by-label [^Array v _]    (vec (.getArray v)))
  (read-column-by-index [^Array v _ _]  (vec (.getArray v))))



(defmethod ig/init-key :system/postgres [_ {:keys [url]}]
  (jdbc/get-datasource {:jdbcUrl url
                        :dbtype "postgresql"
                        :classname "org.postgresql.Driver"}))

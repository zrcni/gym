(ns gym.backend.system.postgres
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]))

(defmethod ig/init-key :system/postgres [_ {:keys [url]}]
  (jdbc/get-datasource {:jdbcUrl url
                        :dbtype "postgresql"
                        :classname "org.postgresql.Driver"}))

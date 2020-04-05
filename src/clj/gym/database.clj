(ns gym.database
  (:require
   [gym.config :as cfg]
   [next.jdbc :as jdbc]))

(def db {:dbtype "postgresql"
         :classname "org.postgresql.Driver"
         :jdbcUrl cfg/jdbc-database-url})

(defn get-db []
  (jdbc/get-datasource db))

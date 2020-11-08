(ns gym.database
  (:require [gym.config :as cfg]
            [next.jdbc :as jdbc]))

(def db-config {:dbtype "postgresql"
         :classname "org.postgresql.Driver"
         :jdbcUrl cfg/jdbc-database-url})

(def db-conn (jdbc/get-datasource db-config))

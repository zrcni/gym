(ns gym.database
  (:require
   [gym.config :as cfg]
   [next.jdbc :as jdbc]))

;; clojure.java.jdbc config map
(def db {:dbtype "postgresql"
         :classname "org.postgresql.Driver"
         :dbname cfg/pg-db
         :host cfg/pg-host
         :port cfg/pg-port
         :user cfg/pg-user
         :password cfg/pg-password})

(defn get-db []
  (jdbc/get-datasource db))

(ns gym.database
  (:require  [next.jdbc :as jdbc]))

;; clojure.java.jdbc config map
(def db {:dbtype "postgresql"
         :classname "org.postgresql.Driver"
         :dbname "postgres"
         :host "localhost"
         :port 5432
         :user "postgres"
         :password "postgres"})

(defn get-db []
  (jdbc/get-datasource db))

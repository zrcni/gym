(ns gym.config)

(def frontend-url (System/getenv "FRONTEND_URL"))
(def jdbc-database-url (System/getenv "JDBC_DATABASE_URL"))
(def port (System/getenv "PORT"))
(def host-url (System/getenv "HOST_URL"))

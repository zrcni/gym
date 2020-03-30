(ns gym.config
  (:require [environ.core :refer [env]]))

(def pg-host (or (:pg-host env) "localhost"))
(def pg-port (or (:pg-port env) 5432))
(def pg-db (or (:pg-db env) "postgres"))
(def pg-user (or (:pg-user env) "postgres"))
(def pg-password (or (:pg-password env) "postgres"))
(def frontend-url (or (:pg-frontend-url env) "http://localhost:3449"))

(ns gym.users.repository.core
  (:require [gym.database :refer [db-conn]]
            [gym.users.repository.postgresql-user-repository :refer [create-postgresql-user-repository]]))

(def user-repository (create-postgresql-user-repository db-conn))

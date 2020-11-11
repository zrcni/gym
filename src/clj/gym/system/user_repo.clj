(ns gym.system.user-repo
  (:require [integrant.core :as ig]
            [gym.users.repository.postgresql-user-repository :refer [create-postgresql-user-repository]]))

(defmethod ig/init-key :system/user-repo [_ {:keys [postgres]}]
  (create-postgresql-user-repository postgres))

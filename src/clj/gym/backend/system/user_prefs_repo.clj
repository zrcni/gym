(ns gym.backend.system.user-prefs-repo
  (:require [integrant.core :as ig]
            [gym.backend.user-prefs.repository.postgresql-user-prefs-repository :refer [create-postgresql-user-prefs-repository]]))

(defmethod ig/init-key :system/user-prefs-repo [_ {:keys [postgres]}]
  (create-postgresql-user-prefs-repository postgres))

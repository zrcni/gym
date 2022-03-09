(ns gym.backend.user-prefs.get-user-preferences
  (:require [gym.backend.user-prefs.repository.user-prefs-repository :refer [get-by-user-id]]
            [gym.backend.user-prefs.core :refer [create-user-prefs]]
            [gym.util :refer [create-uuid]]))

(defn controller [req]
  (let [repo (-> req :deps :user-prefs-repo)
        user-id (create-uuid (get-in req [:user :user_id]))
        prefs (get-by-user-id repo user-id) 
        prefs (or prefs (create-user-prefs user-id))]

    {:status 200
     :body prefs}))

(ns gym.backend.user-prefs.update-user-preferences
  (:require [gym.backend.logger :as log]
            [gym.backend.user-prefs.repository.user-prefs-repository :refer [get-by-user-id save!]]
            [gym.backend.user-prefs.core :refer [update-user-prefs create-user-prefs]]
            [clojure.walk :refer [keywordize-keys]]
            [gym.util :refer [create-uuid]]))

(defn format-prefs [prefs]
  (select-keys prefs [:theme_main_color
                      :excluded_tags]))

(defn controller [req]
  (let [repo (-> req :deps :user-prefs-repo)
        user-id (get-in req [:user :user_id])
        params (-> req :body keywordize-keys)
        prefs (get-by-user-id repo (create-uuid user-id))
        updated-prefs (-> (or prefs (create-user-prefs user-id))
                          (update-user-prefs params))]

    (when-not (= prefs updated-prefs)
      (save! repo updated-prefs))

    (log/info "user preferences updated" {:user-id user-id})

    {:status 200
     :body (format-prefs updated-prefs)}))

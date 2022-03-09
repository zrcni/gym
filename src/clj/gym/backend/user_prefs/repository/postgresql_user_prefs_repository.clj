(ns gym.backend.user-prefs.repository.postgresql-user-prefs-repository
  (:require [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as jdbc-sql]
            [honey.sql.helpers :as h]
            [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [gym.util :refer [create-uuid]]
            [gym.backend.user-prefs.repository.user-prefs-repository :refer [UserPrefsRepository]]))

(defn user-prefs-query [user-id]
  (-> (h/select :*)
      (h/from :public.user_preferences)
      (h/where [:= :user_id user-id])
      (sql/format)))

(defn row->prefs [row]
  (-> row
      (assoc :user_id (str (:user_id row)))))

(defn prefs->row [prefs]
  (-> prefs
      (assoc :user_id (create-uuid (:user_id prefs)))
      (assoc :excluded_tags (into-array String (:excluded_tags prefs)))))



(defrecord PostgresqlUserPrefsRepository [db-conn]
  UserPrefsRepository

  (get-by-user-id
    [_ user-id]
    (let [[prefs] (jdbc-sql/query db-conn
                                  (user-prefs-query user-id)
                                  {:builder-fn rs/as-unqualified-maps})]
      (if prefs
        (row->prefs prefs)
        nil)))
  
  (save!
    [_ prefs]
   (let [row (prefs->row prefs)]
     (jdbc/with-transaction [tx db-conn]
       (if (first (jdbc-sql/query tx (user-prefs-query (:user_id row))))
         (jdbc-sql/update! tx
                           "user_preferences"
                           row
                           ["user_id = ?" (:user_id row)])
         (jdbc-sql/insert! tx
                           "user_preferences"
                           row
                           {:builder-fn rs/as-unqualified-maps}))
       nil))))



(defn create-postgresql-user-prefs-repository [db-conn]
  (->PostgresqlUserPrefsRepository db-conn))

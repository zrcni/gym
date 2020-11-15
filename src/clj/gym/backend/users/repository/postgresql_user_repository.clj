(ns gym.backend.users.repository.postgresql-user-repository
  (:require [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [gym.backend.users.repository.user-repository :refer [UserRepository]]))

(defn users-query [& [where limit]]
 (let [where-clause (if where (str " WHERE " where " ") "")
       limit-clause (if limit (str " LIMIT " limit) "")]
   (str "SELECT user_id, token_user_id, avatar_url, username"
        " FROM users"
        where-clause
        limit-clause)))

(defn row->user [row]
  (-> row
      (assoc :user_id (str (:user_id row)))))



(defrecord PostgresqlUserRepository [db-conn]
  UserRepository

  (create-user!
   [this {:keys [token-user-id username avatar-url]}]
   (let [new-user (sql/insert! db-conn
                               "users"
                               {:token_user_id token-user-id
                                :username username
                                :avatar_url avatar-url}
                               {:return-keys true
                                :builder-fn rs/as-unqualified-maps})]
     (row->user new-user)))

  (get-user-by-token-user-id
   [this token_user_id]
   (let [users (sql/query db-conn
                          [(users-query "token_user_id = ?" 1) token_user_id]
                          {:builder-fn rs/as-unqualified-maps})]
     (if (> (count users) 0)
       (row->user (first users))
       nil))))



(defn create-postgresql-user-repository [db-conn]
  (->PostgresqlUserRepository db-conn))

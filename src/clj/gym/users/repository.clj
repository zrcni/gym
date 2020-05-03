(ns gym.users.repository
  (:require
   [gym.database :refer [get-db]]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]))

(defn users-query [& [where limit]]
 (let [where-clause (if where (str " WHERE " where " ") "")
       limit-clause (if limit (str " LIMIT " limit) "")]
   (str "SELECT user_id, token_user_id, avatar_url, username"
        " FROM users"
        where-clause
        limit-clause)))

(defn row->user [row] row)

(defn create! [{:keys [token_user_id username avatar_url]}]
  (let [new-user (sql/insert! (get-db)
                              "users"
                              {:token_user_id token_user_id
                               :username username
                               :avatar_url avatar_url}
                              {:return-keys true
                               :builder-fn rs/as-unqualified-maps})]
    (row->user new-user)))

(defn get-by-token-user-id [token_user_id]
  (let [users (sql/query (get-db)
                        [(users-query "token_user_id = ?" 1) token_user_id])]
    (if (> (count users) 0)
      (row->user users)
      nil)))

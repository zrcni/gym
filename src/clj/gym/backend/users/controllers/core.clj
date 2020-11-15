(ns gym.backend.users.controllers.core
  (:require [gym.backend.users.controllers.get-authenticated-user :as get-authenticated-user-controller]))

(defn get-authenticated-user [user-repository]
  (get-authenticated-user-controller/create user-repository))

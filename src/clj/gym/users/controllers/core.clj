(ns gym.users.controllers.core
  (:require [gym.users.controllers.get-authenticated-user :as get-authenticated-user-controller]))

(def get-authenticated-user (get-authenticated-user-controller/create))

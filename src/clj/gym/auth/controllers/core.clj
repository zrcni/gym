(ns gym.auth.controllers.core
  (:require [gym.auth.controllers.login :as login-controller]))

(defn login [user-repository]
  (login-controller/create user-repository))

(ns gym.backend.auth.controllers.core
  (:require [gym.backend.auth.controllers.login :as login-controller]))

(defn login [user-repository]
  (login-controller/create user-repository))

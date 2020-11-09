(ns gym.auth.controllers.core
  (:require [gym.auth.controllers.login :as login-controller]))

(def login (login-controller/create))

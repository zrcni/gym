(ns gym.config
  (:require [environ.core :refer [env]]))

(def frontend-url (:frontend-url env))
(def jdbc-database-url (:jdbc-database-url env))
(def port (:port env))
(def host-url (:host-url env))

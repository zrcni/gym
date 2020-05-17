(ns gym.config
  (:require [environ.core :refer [env]]
            [clojure.string :refer [split]]))

(def frontend-urls (split (:frontend-urls env) #","))
(def jdbc-database-url (:jdbc-database-url env))
(def port (:port env))
(def host-url (:host-url env))
(def public-key (:public-key env))
(def commit-sha (:commit-sha env))

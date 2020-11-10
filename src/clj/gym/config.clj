(ns gym.config
  (:import java.util.Locale)
  (:require [environ.core :refer [env]]
            [clojure.string :refer [split]]))

(def dev? (:dev env))
(def frontend-urls (split (:frontend-urls env) #","))
(def jdbc-database-url (:jdbc-database-url env))
(def port (:port env))
(def host-url (:host-url env))
(def public-key (:public-key env))
(def commit-sha (:commit-sha env))
(def redis-url (:redis-url env))

(Locale/setDefault (Locale. "fi" "FI"))

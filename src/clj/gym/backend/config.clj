(ns gym.backend.config
  (:import java.util.Locale)
  (:require [environ.core :refer [env]]
            [clojure.string :refer [split]]))

(def dev? (= (:dev env) "true"))
(def frontend-urls (split (:frontend-urls env) #","))
(def jdbc-database-url (:jdbc-database-url env))
(def port (when-not (= "" (:port env)) (Integer/parseInt (:port env))))
(def host-url (:host-url env))
(def public-key (:public-key env))
(def commit-sha (:commit-sha env))

(Locale/setDefault (Locale. "fi" "FI"))

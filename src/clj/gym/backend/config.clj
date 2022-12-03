(ns gym.backend.config
  (:import java.util.Locale)
  (:require [environ.core :refer [env]]
            [clojure.string :refer [split]]))

(def dev? (= (:dev env) "true"))
(def frontend-urls (if (:frontend-urls env) (split (:frontend-urls env) #",") []))
(def jdbc-database-url (or (:jdbc-database-url env) ""))
(def port (if (:port env) (Integer/parseInt (:port env)) -1))
(def host-url (or (:host-url env) ""))
(def auth0-public-key (or (:auth0-public-key env) ""))
(def auth0-client-id (or (:auth0-client-id env) ""))
(def auth0-domain (or (:auth0-domain env) ""))
(def commit-sha (or (:commit-sha env) ""))
(def sentry-dsn (or (:sentry-dsn env) ""))

(Locale/setDefault (Locale. "fi" "FI"))

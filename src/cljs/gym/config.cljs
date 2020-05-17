(ns gym.config
  (:require [cljs.core :refer [goog-define]]))

(goog-define commit-sha "")
(goog-define sentry-dsn false)
(goog-define api-url "http://localhost:3001")
(goog-define auth0-client-id "")
(def debug? ^boolean goog.DEBUG)

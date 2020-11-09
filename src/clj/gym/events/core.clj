(ns gym.events.core
  (:require [gym.events.domain-events :refer [create-domain-events]]))

(def domain-events (create-domain-events (atom {})))

(ns gym.backend.system.domain-events
  (:require [integrant.core :as ig]
            [gym.backend.domain-events :refer [create-domain-events]]))

(defmethod ig/init-key :system/domain-events [_ _]
  (create-domain-events (atom {})))

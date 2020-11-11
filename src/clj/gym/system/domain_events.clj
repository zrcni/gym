(ns gym.system.domain-events
  (:require [integrant.core :as ig]
            [gym.domain-events :refer [create-domain-events]]))

(defmethod ig/init-key :system/domain-events [_ _]
  (create-domain-events (atom {})))

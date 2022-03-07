(ns gym.backend.system.handler
  (:require [integrant.core :as ig]
            [gym.backend.routes :as routes]))

(defmethod ig/init-key :system/handler [_ deps]
  (routes/create-routes deps))

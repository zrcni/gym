(ns gym.system.redis
  (:require [integrant.core :as ig]))

(defmethod ig/init-key :system/redis [_ {:keys [url]}]
  {:pool {}
   :spec {:uri url}})

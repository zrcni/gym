(ns gym.backend.main
  (:require [gym.backend.system :refer [start-system!]]
            [gym.backend.logger :as log]
            [gym.backend.config :as cfg])
  (:gen-class))

(log/update-context #(assoc % :commit cfg/commit-sha))

(defn -main [& _args]
  (start-system!))

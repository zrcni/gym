(ns gym.backend.main
  (:require [gym.backend.system :refer [start-system!]]
            [gym.backend.logger :as log]
            [gym.backend.config :as cfg])
  (:gen-class))

(defn -main [& _args]
  (log/update-context #(assoc % :commit cfg/commit-sha))
  (start-system!))

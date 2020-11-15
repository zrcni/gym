(ns gym.backend.main
  (:require [gym.backend.workouts.counters.reinitialize-counters :as reinitialize-counters]
            [gym.backend.system.core :refer [start-system!]])
  (:gen-class))

(defn -main [& _args]
  (let [system (start-system!)]
    (reinitialize-counters/exec system)))

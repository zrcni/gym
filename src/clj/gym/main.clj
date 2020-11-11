(ns gym.server
  (:require [gym.workouts.counters.reinitialize-counters :as reinitialize-counters]
            [gym.system.core :refer [start-system!]])
  (:gen-class))

(defn -main [& _args]
  (let [system (start-system!)]
    (reinitialize-counters/exec system)))

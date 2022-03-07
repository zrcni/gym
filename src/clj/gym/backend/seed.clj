(ns gym.backend.seed
  (:require [gym.backend.system :refer [start-system! stop-system! default-config]]
            [gym.backend.workouts.counters.reinitialize-counters :as reinitialize-counters]))

(defn -main [& _args]
  (let [config (select-keys default-config [:system/redis
                                            :system/postgres
                                            :system/workout-duration-counter-weekly
                                            :system/workout-duration-counter-monthly])
        system (start-system! config)]

    (try
      (reinitialize-counters/exec system)
      (println "successfully reinitialized counters")
      (catch Exception err
        (println "failed to reinitialize counters: " err))
      (finally (stop-system! system)))))

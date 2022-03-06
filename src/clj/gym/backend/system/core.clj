(ns gym.backend.system.core
  (:require [gym.backend.config :as cfg]
            [integrant.core :as ig]
            [gym.backend.system.domain-events]
            [gym.backend.system.handler]
            [gym.backend.system.postgres]
            [gym.backend.system.redis]
            [gym.backend.system.server]
            [gym.backend.system.subscriptions]
            [gym.backend.system.user-repo]
            [gym.backend.system.workout-duration-counter-monthly]
            [gym.backend.system.workout-duration-counter-weekly]
            [gym.backend.system.workout-repo]))

(def default-config
  {:system/postgres {:url cfg/jdbc-database-url}

   :system/redis {:url cfg/redis-url}

   :system/domain-events {}

   :system/subscriptions {:domain-events (ig/ref :system/domain-events)
                          :workout-duration-counter-weekly (ig/ref :system/workout-duration-counter-weekly)
                          :workout-duration-counter-monthly (ig/ref :system/workout-duration-counter-monthly)}

   :system/user-repo {:postgres (ig/ref :system/postgres)}

   :system/workout-repo {:postgres (ig/ref :system/postgres)
                         :domain-events (ig/ref :system/domain-events)}

   :system/workout-duration-counter-weekly {:redis (ig/ref :system/redis)}

   :system/workout-duration-counter-monthly {:redis (ig/ref :system/redis)}

   :system/handler {:user-repo (ig/ref :system/user-repo)
                    :workout-repo (ig/ref :system/workout-repo)
                    :workout-duration-counter-weekly (ig/ref :system/workout-duration-counter-weekly)
                    :workout-duration-counter-monthly (ig/ref :system/workout-duration-counter-monthly)
                    :postgres (ig/ref :system/postgres)}

   :system/server {:handler (ig/ref :system/handler)
                   :opts {:port cfg/port
                          :auto-reload? cfg/dev?}}})

(defn start-system!
  ([] (start-system! default-config))
  ([config]
   (let [system (ig/init config)]
     (println "System started!")
     system)))

(defn stop-system! [system]
  (ig/halt! system)
  (println "System stopped!"))

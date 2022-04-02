(ns gym.backend.system
  (:require [gym.backend.logger :as log]
            [gym.backend.config :as cfg]
            [integrant.core :as ig]
            [gym.backend.system.handler]
            [gym.backend.system.postgres]
            [gym.backend.system.server]
            [gym.backend.system.user-repo]
            [gym.backend.system.user-prefs-repo]
            [gym.backend.system.workout-repo]))

(def default-config
  {:system/postgres {:url cfg/jdbc-database-url}

   :system/user-repo {:postgres (ig/ref :system/postgres)}

   :system/user-prefs-repo {:postgres (ig/ref :system/postgres)}

   :system/workout-repo {:postgres (ig/ref :system/postgres)}

   :system/handler {:user-repo (ig/ref :system/user-repo)
                    :user-prefs-repo (ig/ref :system/user-prefs-repo)
                    :workout-repo (ig/ref :system/workout-repo)
                    :postgres (ig/ref :system/postgres)}

   :system/server {:handler (ig/ref :system/handler)
                   :opts {:port cfg/port
                          :auto-reload? cfg/dev?}}})

(defn start-system!
  ([] (start-system! default-config))
  ([config]
   (let [system (ig/init config)]
     (log/info "system started" {:components (keys config)})
     system)))

(defn stop-system! [system]
  (ig/halt! system)
  (log/info "system stopped"))

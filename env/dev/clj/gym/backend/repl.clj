(ns gym.backend.repl
  (:require [gym.backend.system :refer [default-config]]
            [integrant.repl :refer [go reset]]
            [gym.backend.logger :as log]
            [gym.backend.config :as cfg]))

(log/update-context #(assoc % :commit cfg/commit-sha))

;; Starts the system
;; System can be reloaded after changing any code
;; by invoking (reset) in the REPL

(integrant.repl/set-prep! (constantly default-config))

(defn restart []
  (reset)
  (go))

(comment

  (go)

  (restart))

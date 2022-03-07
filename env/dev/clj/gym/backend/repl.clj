(ns gym.backend.repl
  (:require [gym.backend.system :refer [default-config]]
            [integrant.repl :refer [go reset]]))

;; Starts the system
;; System can be reloaded after changing any code
;; by invoking (reset) in the REPL

(integrant.repl/set-prep! (constantly default-config))

(comment

  (go)

  (reset))
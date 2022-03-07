(ns gym.backend.main
  (:require [gym.backend.system :refer [start-system!]])
  (:gen-class))

(defn -main [& _args]
  (start-system!))

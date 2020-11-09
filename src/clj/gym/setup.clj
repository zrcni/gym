(ns gym.setup
  (:import java.util.Locale)
  (:require [gym.config]
            [gym.stats.counters.reinitialize-counters]))

(Locale/setDefault (Locale. "fi" "FI"))


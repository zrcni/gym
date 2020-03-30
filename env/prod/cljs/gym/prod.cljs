(ns gym.prod
  (:require [gym.core :as core]))

;; TODO: create logger module for info/error/warn/debug logs
;;ignore println statements in prod
;; (set! *print-fn* (fn [& _]))

(core/init!)

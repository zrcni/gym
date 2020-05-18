(ns gym.prod
  (:require
   [gym.core :as core]
   [re-frisk.core :refer [enable-re-frisk!]]))

;; TODO: create logger module for info/error/warn/debug logs
;;ignore println statements in prod
;; (set! *print-fn* (fn [& _]))

;; TODO: remove refrisk in prod
(enable-re-frisk!)

(core/init!)

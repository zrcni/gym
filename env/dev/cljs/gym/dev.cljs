(ns ^:figwheel-no-load gym.dev
  (:require
   [gym.core :as core]
   [re-frisk.core :refer [enable-re-frisk!]]
   [devtools.core :as devtools]))

(extend-protocol IPrintWithWriter
  js/Symbol
  (-pr-writer [sym writer _]
    (-write writer (str "\"" (.toString sym) "\""))))

(devtools/install!)

(enable-console-print!)
(enable-re-frisk!)

(core/init!)

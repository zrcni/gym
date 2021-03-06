(ns ^:figwheel-no-load gym.frontend.dev
  (:require
   [gym.frontend.core :as core]
   [re-frisk.core :refer [enable-re-frisk!]]
   [devtools.core :as devtools]))

(extend-protocol IPrintWithWriter
  js/Symbol
  (-pr-writer [sym writer _]
    (-write writer (str "\"" (str sym) "\""))))

(devtools/install!)

(enable-console-print!)
(enable-re-frisk!)

(core/init!)

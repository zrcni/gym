(ns ^:figwheel-no-load gym.frontend.dev
  (:require
   [gym.frontend.core :as core]
   [re-frame.db :refer [app-db]]
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

(comment
  (let [data (-> @app-db :analytics :results :workouts-by-day-of-week :data)]
    (into #{} (map :tag data))
    #_(->> data
         (group-by :date)
         (mapv (fn [[date maps]]
                 (reduce (fn [acc {:keys [tag count]}]
                           (assoc acc tag count))
                         {:date date}
                         maps)))))
         )
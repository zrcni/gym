(ns gym.server
  (:require [gym.handlers.api :as api-handler]
            [gym.config :as cfg]
            [ring.adapter.jetty :refer [run-jetty]]
            [gym.subscriptions :as subscriptions]
            [gym.stats.counters.reinitialize-counters :as reinitialize-counters])
  (:gen-class))

;; (defn start-web []
;;   (let [web-port (or (env :port) 3000)]
;;     (run-jetty web-handler {:port web-port :join? false})))

(defn start-api []
  (let [port (Integer/parseInt cfg/port)]
    (println (str "Starting server on port " port))
    (run-jetty api-handler/handler {:port port :join? true})))

(defn -main [& _args]
  (subscriptions/register)
  (reinitialize-counters/exec)
  ;; (start-web)
  (start-api))

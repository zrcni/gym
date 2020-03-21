(ns gym.server
    (:require
     [gym.handler :refer [web-handler api-handler]]
     [config.core :refer [env]]
     [ring.adapter.jetty :refer [run-jetty]])
    (:gen-class))

;; I think these are only supported to be called in prod? maybe? cause gym.repl namespace has dev server start

(defn start-web []
  (let [web-port (or (env :port) 3000)]
    (run-jetty web-handler {:port web-port :join? false})))

(defn start-api []
  (let [api-port (or (env :api-port) 3001)]
    (run-jetty api-handler {:port api-port :join? false})))

(defn -main [& args]
  (start-web)
  (start-api))

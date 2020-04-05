(ns gym.server
    (:require
     [gym.handler :refer [web-handler api-handler]]
     [gym.config :as cfg]
     [ring.adapter.jetty :refer [run-jetty]])
    (:gen-class))

;; (defn start-web []
;;   (let [web-port (or (env :port) 3000)]
;;     (run-jetty web-handler {:port web-port :join? false})))

(defn start-api []
    (run-jetty api-handler {:port (Integer/parseInt cfg/server-port) :join? false}))

(defn -main [& args]
  ;; (start-web)
  (start-api))

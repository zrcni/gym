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
  (let [port (Integer/parseInt cfg/port)]
    (println (str "Starting server on port " port))
    (run-jetty api-handler {:port port :join? true})))

(defn -main [& args]
  ;; (start-web)
  (start-api)
  (println "no, not here"))

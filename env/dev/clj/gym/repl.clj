(ns gym.repl
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [gym.config :as cfg]
            [gym.handlers.api :as api-handler]
            [gym.handlers.web :as web-handler]
            [gym.subscriptions])
  (:use figwheel-sidecar.repl-api
        ring.server.standalone
        [ring.middleware file-info file]))

(defonce server (atom nil))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'web-handler/handler
      ; Makes static assets in $PROJECT_DIR/resources/public/ available.
      (wrap-file "resources")
      ; Content-Type, Content-Length, and Last Modified headers for files in body
      (wrap-file-info)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (reset! server
            (serve (get-handler)
                   {:port port
                    :auto-reload? true
                    :join? false}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))

(defonce api-server (atom nil))

(defn start-api-server [& [port]]
    (reset! server
            (run-jetty #'api-handler/handler {:port (Integer/parseInt port)
                                    :auto-reload? true
                                    :join? false}))
    (println (str "API server listening on port " port)))

(defn stop-api-server []
  (.stop @api-server)
  (reset! api-server nil))

(start-api-server cfg/port)

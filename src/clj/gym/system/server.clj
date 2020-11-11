(ns gym.system.server
  (:require [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]))

(defmethod ig/init-key :system/server [_ {:keys [handler opts]}]
  (let [handler (atom (delay handler))
        options (assoc opts :join? false)]
    {:handler handler
     :server  (jetty/run-jetty (fn [req] (@@handler req)) options)}))

(defmethod ig/halt-key! :system/server [_ {:keys [server]}]
  (.stop server))

;; suspend-key! doesn't seem to be invoked when
;; resetting the system using integrant.repl/reset.
;; I implemented this based on this integrant docs:
;; https://github.com/weavejester/integrant
(defmethod ig/resume-key :system/server [key opts old-opts old-impl]
  (if (= (:opts opts) (:opts old-opts))
    old-impl
    (do (ig/halt-key! key old-impl)
        (ig/init-key key opts))))

(defmethod ig/resolve-key :system/server [_ {:keys [server]}]
  server)

;; (defmethod ig/suspend-key! :system/server [_ {:keys [handler]}]
;;   (reset! handler (promise)))

;; (defmethod ig/resume-key :system/server [key opts old-opts old-impl]
;;   (if (= (:opts opts) (:opts old-opts))
;;     (do (deliver @(:handler old-impl) (:handler opts))
;;         old-impl)
;;     (do (ig/halt-key! key old-impl)
;;         (ig/init-key key opts))))

;; (defmethod ig/resolve-key :system/server [_ {:keys [server]}]
;;   server)

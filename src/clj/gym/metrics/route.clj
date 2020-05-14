(ns gym.metrics.route
  (:require [gym.middleware :refer [wrap-user]]))

(defn augment-event [event request]
  (let [headers (:headers request)]
    (-> event
        (assoc :user-id (-> request :context :user :user_id))
        (assoc :origin (get headers "origin"))
        (assoc :referer (get headers "referer"))
        (assoc :user-agent (get headers "user-agent")))))

(defn handle-send-metrics [request]
  (let [events (get-in request [:body "events"])]

    (if (and events (> (count events) 0))
      ;; TODO: queue events to be saved
      (do (println "Events: " (vec (map #(augment-event % request) events)))
          {:status 204})

      {:status 400
       :body {:error "Invalid body format. Missing key: events"}})))

(defn create-metrics-route [path]
  [path {:post {:handler handle-send-metrics
                :middleware [wrap-user]}}])

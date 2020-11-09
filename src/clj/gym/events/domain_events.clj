(ns gym.events.domain-events
  (:require [clojure.core.async :refer [chan >! <! go go-loop]]))

(defprotocol DomainEventsAPI
  (dispatch-event [this event])
  (register-event [this event-name])
  (register-global-event [this])
  (subscribe-event [this event-name callback])
  (subscribe-global-event [this callback]))



(defrecord DomainEvents [channels]
  DomainEventsAPI

  (dispatch-event
    [this event]
    "Dispatch the event to all channels for the specific event
    as well as all global channels."
    (go
      (let [event-channels (get @channels (:event-name event))
            global-channels (get @channels :global)]
        (when (or event-channels global-channels)
          (doseq [c (concat event-channels global-channels)]
            (>! c event))))))

  (register-event
    [this event-name]
    "Create a new channel and return it."
    (let [c (chan)]
      (swap! channels update event-name conj c)
      c))

  (register-global-event
    [this]
    (register-event this :global))

  (subscribe-event
    [this event-name callback]
    "Subscribe to events via callback."
    (let [c (register-event this event-name)]
      (go-loop []
        (try
          (callback (<! c))
          (catch Exception err (println err)))
        (recur))))

  (subscribe-global-event
    [this callback]
    (subscribe-event this :global callback)))



(defn create-domain-events [channels-atom]
  (->DomainEvents channels-atom))

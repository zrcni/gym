(ns gym.backend.domain-events
  (:require [clojure.core.async :refer [chan >! <! go go-loop]]
            [gym.backend.date-utils :refer [instant instant?]]
            [clojure.spec.alpha :as s]))

(defprotocol DomainEventsAPI
  (dispatch-event [this event])
  (register-event [this event-name])
  (register-global-event [this])
  (subscribe-event [this event-name callback])
  (subscribe-global-event [this callback]))



(defrecord DomainEvents [channels]
  DomainEventsAPI

  (dispatch-event
   "Dispatch the event to all channels for the specific event
    as well as all global channels."
   [_this event]
   (go
     (let [event-channels (get @channels (:event-name event))
           global-channels (get @channels :global)]
       (when (or event-channels global-channels)
         (doseq [c (concat event-channels global-channels)]
           (>! c event))))))

  (register-event
   "Create a new channel and return it."
   [_this event-name]
   (let [c (chan)]
     (swap! channels update event-name conj c)
     c))

  (register-global-event
    [this]
    (register-event this :global))

  (subscribe-event
   "Subscribe to events via callback."
   [this event-name callback]
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



(s/def :event/event-name keyword?)
(s/def :event/payload map?)
(s/def :event/created-at instant?)

(s/def ::event (s/keys :req-un [:event/event-name
                                :event/payload
                                :event/created-at]))



(defn create-domain-event [event-name payload]
  (let [event {:event-name event-name
               :payload payload
               :created-at (instant)}]
    (if (s/valid? ::event event)
      event
      (throw (ex-info "Invalid event" {:event event})))))

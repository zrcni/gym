(ns gym.events.create-domain-event
  (:require [gym.date-utils :refer [instant instant?]]
            [clojure.spec.alpha :as s]))

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

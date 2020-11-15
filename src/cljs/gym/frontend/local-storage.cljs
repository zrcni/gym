(ns gym.frontend.local-storage
  (:require
   [re-frame.core :refer [reg-event-fx reg-fx]]
   [cljs.reader :as reader]))

(defn ls-set! [k v]
  (.setItem js/localStorage (pr-str k) (pr-str v)))

(defn ls-get [k]
  (when-let [s (.getItem js/localStorage (pr-str k))]
    (reader/read-string s)))

(defn ls-remove! [k]
  (.removeItem js/localStorage k))

(reg-fx :set-local-storage!
        (fn [[key value]]
          (ls-set! key value)))

(reg-fx :remove-local-storage!
        (fn [[key]]
          (ls-remove! key)))

(reg-event-fx :set-local-storage
              (fn [_ [_ params]]
                {:set-local-storage! params}))

(reg-event-fx :remove-local-storage
              (fn [_ [_ params]]
                {:remove-local-storage! params}))

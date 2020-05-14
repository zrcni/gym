(ns gym.metrics
  (:require
   [fingerprintjs2]
   [clojure.string :refer [join]]
   [cljs.core :refer [random-uuid]]
   [gym.config :as cfg]
   [clojure.core.async :as async :refer [timeout chan put! go <! >!]]
   [re-frame.core :refer [reg-event-fx reg-fx dispatch]]
   [ajax.core :refer [json-request-format]]
   [gym.local-storage :refer [ls-set! ls-get]]))

(def ^:private metrics-url (str cfg/api-url "/api/metrics"))
(def ^:private client-id "exercise-tracker-web")
;; initialize with device value from local storage, because page load events might now have it asap
(def ^:private device-fingerprint (atom (ls-get :device-fingerprint)))
(def ^:private event-in-chan (chan))
(def ^:private event-out-chan (chan))

(defn ^:private resolution [x y]
  (str x "x" y))

(defn ^:private make-with-client-details [{:keys [document window navigator device-fingerprint]}]
  (fn [event]
    (-> event
        (assoc :page-url (-> window .-location .-href))
        (assoc :page-title (-> document .-title))
        (assoc :browser-language (-> navigator .-language))
        (assoc :screen-resolution (resolution (-> window .-screen .-width)
                                              (-> window .-screen .-height)))
        (assoc :window-resolution (resolution (-> window .-innerWidth)
                                              (-> window .-innerHeight)))
        (assoc :device-fingerprint device-fingerprint)
        (assoc :cookies-enabled (-> navigator .-cookieEnabled))
        (assoc :online (-> navigator .-onLine)))))

(def ^:private with-client-details (make-with-client-details {:document js/document
                                                              :window js/window
                                                              :navigator js/navigator
                                                              :device-fingerprint @device-fingerprint}))


(defn ^:private with-db-data [event db]
  event)

(defn ^:private augment-event [{:keys [event-name initiator event-details]} db]
  (-> {:event-name event-name
       :event-id (.toString (random-uuid))
       :initiator initiator
       :client client-id
       :platform "web"
       :event-details event-details
       :created-timestamp (-> js/Date new .toISOString)}

      (with-client-details)
      (with-db-data db)))

(defn ^:private with-sent-timestamp [event]
  (assoc event :sent-timestamp (-> js/Date new .toISOString)))

(defn ^:private create-user-event
  ([event-name] (create-user-event event-name nil))
  ([event-name event-details] {:event-name event-name
                               :event-details event-details
                               :initiator "user"}))

(defn ^:private create-app-event
  ([event-name] (create-app-event event-name nil))
  ([event-name event-details] {:event-name event-name
                               :event-details event-details
                               :initiator "app"}))

;; loops indefinitely
;; starts another loop after max-time or max-count
;; TODO: integrate debouncing - what I mean is start the loop on first invocation and stop after timeout
(defn ^:private batch [in out max-time max-count]
  (let [limit (dec max-count)]
    (async/go-loop [buf []
                    t (timeout max-time)]
      (let [[v p] (async/alts! [in t])]
        (cond
          (= p t)
          (do
            (when (> (count buf) 0) (>! out buf))
            (recur [] (timeout max-time)))

          (nil? v)
          (when (seq buf)
            (>! out buf))

          (>= (count buf) limit)
          (do
            (>! out (conj buf v))
            (recur [] (timeout max-time)))

          :else
          (recur (conj buf v) t))))))

(def ^:private events-send-chan (chan))

(defn ^:private enqueue [event]
  (put! event-in-chan event))

(reg-fx ::enqueue-event!
              (fn [event]
                (enqueue event)))

(reg-event-fx ::augment-event
              (fn [{:keys [db]} [_ event]]
               {::enqueue-event! (augment-event event db)}))

(reg-event-fx ::user-event
              (fn [_ [_ event-name event-details]]
                {:dispatch [::augment-event (create-user-event event-name event-details)]}))

(reg-event-fx ::app-event
              (fn [_ [_ event-name event-details]]
                {:dispatch [::augment-event (create-app-event event-name event-details)]}))

(reg-event-fx ::send-metrics
              (fn [_ [_ events]]
                {:dispatch [:fetch {:method :post
                                    :params {:events events}
                                    :format (json-request-format)
                                    :uri (str cfg/api-url "/api/metrics")}]}))

(.get fingerprintjs2 (fn [^FingerPrintComponents components]
                       (let [stringified-components (join (map #(.-value %) components) "")
                             fingerprint-hash (.x64hash128 fingerprintjs2 stringified-components 31)]
                         (reset! device-fingerprint fingerprint-hash)
                         (ls-set! :device-fingerprint fingerprint-hash))))

(go (while true
      (>! event-out-chan (<! event-in-chan))))

(go (while true
      (dispatch [::send-metrics (<! events-send-chan)])))

;; (go (while true
;;       (send (<! events-send-chan))))

(batch event-out-chan events-send-chan 2000 20)
(ns gym.jwt
  (:import java.time.format.DateTimeFormatter
           java.util.Locale
           java.time.ZoneId
           java.time.Instant)
  (:require [clj-http.client :as http]))

(def ^:private cert (atom nil))
(def ^:private expires-at (atom nil))

(def ^:private cert-url "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com")

(defn ^:private fetch-certificate []
  (http/get cert-url {:as :json}))

(defn ^:private get-expires-at [response]
  (get-in response [:headers "expires"]))

(defn ^:private get-certificate [response-body]
  (let [first-key (first (keys response-body))]
    (get response-body first-key)))

(def ^:private http-date-format (-> (DateTimeFormatter/ofPattern "EEE, dd MMM yyyy HH:mm:ss z" Locale/ENGLISH)
                                    (.withZone (ZoneId/of "GMT"))))

(defn ^:private parse-http-date [date-string]
  (-> (.parse http-date-format date-string)
      (Instant/from)))

(defn ^:private refresh-certificate []
  (println "Refreshing certificate")
  (let [response (fetch-certificate)]
    (if (= 200 (:status response))
      (do
        (reset! expires-at (-> response get-expires-at parse-http-date))
        (reset! cert (-> response :body get-certificate)))
      (delay refresh-certificate 100))))

(defn ^:private has-expired [instant]
  (or
   (not instant)
   (>= 0 (.compareTo instant (Instant/now)))))

(defn get-token []
  (when (or (not @cert) (has-expired @expires-at))
    (refresh-certificate))
  @cert)

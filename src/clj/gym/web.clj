(ns gym.web
  (:require
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]
   [gym.database :refer [db]]))

(prn db)

(def ^{:private true} mount-target
  [:div#app
   [:h2 "Welcome to gym"]
   [:p "please wait while Figwheel is waking up ..."]
   [:p "(Check the js console for hints if nothing exciting happens.)"]])

(defn ^{:private true} head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.12.1/css/all.min.css")
   (include-css "https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css")
   (include-css "https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css")
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn ^{:private true} loading-page []
  (html5
   (head)
   [:body.body-container
    mount-target
    (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js")
    (include-js "/js/app.js")]))

(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

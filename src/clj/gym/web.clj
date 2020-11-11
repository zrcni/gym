(ns gym.web
  (:require
   [hiccup.page :refer [include-js include-css html5]]
   [gym.config :as cfg]
   [reitit.ring :as reitit-ring]
   [gym.middleware :refer [web-middlewares]]))

(def mount-target
  [:div#app
   [:h2 "Welcome to gym"]
   [:p "please wait while Figwheel is waking up ..."]
   [:p "(Check the js console for hints if nothing exciting happens.)"]])

(defn head []
  [:head
   [:title "Exercise tracker"]
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   [:link {:rel "manifest" :href "/manifest.webmanifest"}]
   (include-css (if cfg/dev? "/css/site.css" "/css/site.min.css"))
   (include-css "/css/emoji-mart.css")
   (include-css "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.12.1/css/all.min.css")
   (include-css "https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css")
   (include-css "https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css")])

(defn loading-page []
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


;; TODO: move to gym.system.handler
;; and combine into the same handler with the API

(def handler
  (reitit-ring/ring-handler
   (reitit-ring/router
    ["*" {:get {:handler index-handler}}])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware web-middlewares}))


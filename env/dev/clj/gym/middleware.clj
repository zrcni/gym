(ns gym.middleware
  (:require
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [prone.middleware :refer [wrap-exceptions]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(def web-middleware
  [#(wrap-defaults % site-defaults)
   wrap-exceptions
   wrap-reload])

(def api-middleware
  [#(wrap-cors % :access-control-allow-origin #"http://localhost:3449"
               :access-control-allow-methods [:get :put :post :delete])
   wrap-json-response
   wrap-json-body])

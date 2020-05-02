(ns gym.auth
  (:require
   [auth0spa]
   [gym.config :as cfg]
   [re-frame.core :refer [reg-cofx]]))

(defn auth0->user [js-user]
  (let [user (js->clj js-user)]
    {:user-id (get user "sub")
     :username (get user "nickname")
     :avatar (get user "picture")
     :updated-at (get user "updated_at")}))

;; https://auth0.github.io/auth0-spa-js/interfaces/auth0clientoptions.html
(defn create-auth0-client []
  (new js/auth0spa.Auth0Client (clj->js
                                {:domain "samulir.eu.auth0.com"
                                 :client_id cfg/auth0-client-id
                                 :display "popup"
                                 :useRefreshTokens true
                                 :cacheLocation "localstorage"
                                 :redirect_uri (str (-> js/window .-location .-origin) "/auth0_callback")})))

(defn reg-auth0-cofx [client]
  (reg-cofx
   :auth0
   (fn [cofx]
     (assoc cofx :auth0 client))))

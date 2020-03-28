(ns gym.auth
  (:require
   [re-frame.core :refer [dispatch]]))

(defn get-token []
  (-> js/firebase .auth .-currentUser .getIdToken))

(defn parse-firebase-user [user]
  {:last-login-at (.-lastLoginAt user)
   :display-name (.-displayName user)
   :email (.-email user)
   :user-id (.-uid user)
   :avatar-url (.-photoURL user)})

(defn parse-firebase-tokens [token-manager]
  {:access-token (.-accessToken token-manager)
   :refresh-token (.-refreshToken token-manager)
   :api-key (.-apiKey token-manager)
   :expiration-time (.-expirationTime token-manager)})

(def auth-config {:signInSuccessUrl "http://localhost:3449/login_success"
                  :signInOptions [js/firebase.auth.GoogleAuthProvider.PROVIDER_ID]
                  :callbacks {:signInSuccessWithAuthResult (fn [auth-result]
                                                             (let [firebase-user (.-user auth-result)
                                                                   user (parse-firebase-user firebase-user)
                                                                   token (get-token)]
                                                              ;; I haven't yet figured out how to handle promises in an idiomatic way
                                                               (.then token (fn [token] (dispatch [:login-success user token])))))
                              :signInError (fn [error]
                                             (dispatch [:login-failure error]))}})

(def app-config {:apiKey "AIzaSyCIuXmK7vLJZz54YjCpAq1UHFlQyFI_ENg"
                 :authDomain "exercise-tracker-89a06.firebaseapp.com"
                 :databaseURL "https://exercise-tracker-89a06.firebaseio.com"
                 :projectId "exercise-tracker-89a06"
                 :storageBucket "exercise-tracker-89a06.appspot.com"
                 :messagingSenderId "908491193917"
                 :appId "1:908491193917:web:f8dff3079796e753df7454"})

(defn get-firebase-auth-ui []
  (or 
   (js/firebaseui.auth.AuthUI.getInstance)
   (new js/firebaseui.auth.AuthUI (.auth js/firebase))))

(defn init-firebase-app []
  (when (= 0 (count (.-apps js/firebase)))
    (.initializeApp js/firebase (clj->js app-config))))

(defn init-firebase-auth []
  (.start (get-firebase-auth-ui) "#auth-ui" (clj->js auth-config)))

(defn on-login [firebase-user]
  (let [user (parse-firebase-user firebase-user)
        token (get-token)]                                 ;; I haven't yet figured out how to handle promises in an idiomatic way
    (.then token (fn [token] (dispatch [:login-success user token])))))

(defn on-logout [] (dispatch [:logout-success]))

(defn on-auth-state-changed [firebase-user]
  (if firebase-user
    (on-login firebase-user)
    (on-logout)))

(defn start-firebase-auth-state-listener []
  (.onAuthStateChanged (.auth js/firebase) on-auth-state-changed))

;; triggers onAuthStateChanged
(defn firebase-logout []
  (-> js/firebase .auth .signOut))

(ns gym.frontend.router-utils
  (:require
   [re-frame.core :refer [dispatch subscribe]]
   [reitit.frontend.easy :as rfe]))

"Return relative url for given route. Url can be used in HTML links."
(defn href
  ([k]
   (href k nil nil))
  ([k params]
   (href k params nil))
  ([k params query]
   (rfe/href k params query)))

"Navigate on render"
(defn navigate [{:keys [to]}]
  (dispatch [:navigate to])
  (fn [] nil))

"Navigate to /login if logged out"
(defn private-route []
    (fn [& children]
      (let [user @(subscribe [:user])]
        (if-not user
          [navigate {:to :login}]
          [:<> children]))))

"Navigate to / if logged in"
(defn public-route []
    (fn [& children]
      (let [user @(subscribe [:user])]
        (if user
          [navigate {:to :home}]
          [:<> children]))))

(ns gym.router-utils
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
  (let [user @(subscribe [:user])]
    (fn [& children]
      (if-not user
        [navigate {:to :login}]
        [:<> children]))))

"Navigate to / if logged in"
(defn public-route []
  (let [user @(subscribe [:user])]
    (fn [& children]
      (if user
        [navigate {:to :home}]
        [:<> children]))))

(ns gym.router-utils
  (:require
   [reitit.frontend.easy :as rfe]))

"Return relative url for given route. Url can be used in HTML links."
(defn href
  ([k]
   (href k nil nil))
  ([k params]
   (href k params nil))
  ([k params query]
   (rfe/href k params query)))

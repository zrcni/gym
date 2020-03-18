(ns react-cljs.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :refer [dispatch-sync clear-subscription-cache!]]
   [react-cljs.router :as router]))

(defn mount-root []
  (reagent/render [router/root] (.getElementById js/document "app")))

(defn init! []
  (dispatch-sync [:initialize-db])
  (clear-subscription-cache!)
  (router/start!)
  (mount-root))

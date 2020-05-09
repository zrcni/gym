(ns gym.views
  (:require
   [re-frame.core :refer [subscribe dispatch]]))

(defn layout []
  (fn [_ & children]
    (let [user @(subscribe [:user])]
      [:<>
       [:header {:id "header" :class "navbar navbar-expand navbar-dark flex-md-row bd-navbar"}
        [:div.header-left
         [:a.header-title {:href "/"} "Exercise tracker"]]
        [:div.header-right
         (when (:avatar_url user)
           [:img.header-user-avatar {:src (:avatar_url user) :alt "user-logo"}])
         (when user
           [:button.logout-button {:on-click #(dispatch [:logout])} "Logout"])]]
       [:main {:id "content"}
        children]])))

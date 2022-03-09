(ns gym.frontend.theme
  (:require [gym.util :refer [contains-many? inc-by]]
            [gym.frontend.local-storage :refer [ls-get]]
            [parse-color]
            [re-frame.core :refer [reg-event-db reg-event-fx]]))

(defn hsl-vec->css-color [hsl]
  (let [h (get hsl 0)
        s (get hsl 1)
        l (get hsl 2)]
    (str "hsl(" h ", " s "%, " l "%" ")")))

(reg-event-db
 ::set-theme-color
 (fn [db [_ theme-color]]
   (let [hsl (-> theme-color parse-color .-hsl js->clj)
         hover (update hsl 2 (inc-by 12))
         active (update hsl 2 (inc-by 24))]
     (-> db
         (assoc-in [:theme :theme-color] (hsl-vec->css-color hsl))
         (assoc-in [:theme :theme-color-hover] (hsl-vec->css-color hover))
         (assoc-in [:theme :theme-color-active] (hsl-vec->css-color active))))))

(reg-event-fx
 ::update-theme-color
 (fn [{:keys [db]} [_ theme-color]]
   {:dispatch [::set-theme-color theme-color]
    :db (assoc-in db [:theme :preview?] true)}))


(def theme-keys [:theme-color
                 :theme-color-hover
                 :theme-color-active])

(reg-event-fx ::persist-theme
              (fn [{:keys [db]} _]
                {:db (assoc-in db [:theme :preview?] false)
                 :set-local-storage! [:theme (select-keys (:theme db) theme-keys)]}))

(reg-event-db ::initialize
              (fn [db _]
                (let [theme (ls-get :theme)]
                  (if (and theme (contains-many? theme theme-keys))
                    (assoc db :theme theme)
                    db))))

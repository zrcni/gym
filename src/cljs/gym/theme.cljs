(ns gym.theme
  (:require
   [gym.util :refer [contains-many? inc-by]]
   [gym.local-storage :refer [ls-get]]
   [parse-color]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(defn hsl-vec->css-color [hsl]
  (let [h (get hsl 0)
        s (get hsl 1)
        l (get hsl 2)]
    (str "hsl(" h ", " s "%, " l "%" ")")))

(reg-event-fx ::update-accent-color
              (fn [{:keys [db]} [_ accent-color]]
                ;; [h s l]
                (let [hsl (-> accent-color parse-color .-hsl js->clj)
                      hover (update hsl 2 (inc-by 12))
                      active (update hsl 2 (inc-by 24))]
                  {:db (-> db
                           (assoc-in [:theme :accent-color] (hsl-vec->css-color hsl))
                           (assoc-in [:theme :accent-color-hover] (hsl-vec->css-color hover))
                           (assoc-in [:theme :accent-color-active] (hsl-vec->css-color active)))
                   :dispatch [::persist-theme]})))

(reg-event-fx ::persist-theme
              (fn [{:keys [db]} _]
                {:set-local-storage! [:theme (:theme db)]}))

(def req-theme-keys [:accent-color
                     :accent-color-hover
                     :accent-color-active])

(reg-event-db ::initialize
              (fn [db _]
                (let [theme (ls-get :theme)]
                  (if (and theme (contains-many? theme req-theme-keys))
                    (assoc db :theme theme)
                    db))))

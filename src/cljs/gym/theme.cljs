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

(reg-event-fx ::update-theme-color
              (fn [{:keys [db]} [_ theme-color]]
                ;; [h s l]
                (let [hsl (-> theme-color parse-color .-hsl js->clj)
                      hover (update hsl 2 (inc-by 12))
                      active (update hsl 2 (inc-by 24))]
                  {:db (-> db
                           (assoc-in [:theme :theme-color] (hsl-vec->css-color hsl))
                           (assoc-in [:theme :theme-color-hover] (hsl-vec->css-color hover))
                           (assoc-in [:theme :theme-color-active] (hsl-vec->css-color active))
                           (assoc-in [:theme :preview?] true))})))

(reg-event-fx ::persist-theme
              (fn [{:keys [db]} _]
                {:db (assoc-in db [:theme :preview?] false)
                 :set-local-storage! [:theme (:theme db)]}))

(def req-theme-keys [:theme-color
                     :theme-color-hover
                     :theme-color-active])

(reg-event-db ::initialize
              (fn [db _]
                (let [theme (ls-get :theme)]
                  (if (and theme (contains-many? theme req-theme-keys))
                    (assoc db :theme theme)
                    db))))

(ns gym.backend.user-prefs.core)

(defn create-user-prefs [user-id]
  {:user_id user-id
   :excluded_tags []
   :theme_main_color nil})

(defn update-user-prefs [prefs {:keys [theme_main_color excluded_tags]}]
  (cond-> prefs
    theme_main_color
    (assoc :theme_main_color theme_main_color)

    excluded_tags
    (assoc :excluded_tags excluded_tags)))

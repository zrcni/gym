(ns gym.backend.user-prefs.repository.user-prefs-repository)

(defprotocol UserPrefsRepository
  (get-by-user-id [this user-id])
  (save! [this prefs]))

(ns gym.backend.users.repository.user-repository)

(defprotocol UserRepository
  (get-user-by-token-user-id [this token-user-id])
  (create-user! [this params]))

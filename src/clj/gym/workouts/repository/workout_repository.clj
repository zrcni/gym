(ns gym.workouts.repository.workout-repository)

(defprotocol WorkoutRepository
  (get-workouts-by-user-id [this user-id])
  (get-workout-by-workout-id [this workout-id])
  (create-workout! [this params])
  (delete-workout-by-workout-id! [this workout-id]))

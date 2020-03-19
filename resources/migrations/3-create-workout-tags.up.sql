CREATE TABLE workout_tags (
  workout_id uuid NOT NULL REFERENCES "workouts" (workout_id),
  tag varchar(20)
);
--;;
CREATE UNIQUE INDEX "workout_tags_workout_id_tag" on "workout_tags" (workout_id, tag);

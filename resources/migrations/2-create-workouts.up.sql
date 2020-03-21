CREATE TABLE workouts (
  workout_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  description text,
  duration int,
  date date NOT NULL,
  created_at timestamptz NOT NULL DEFAULT current_timestamp,
  modified_at timestamptz NOT NULL DEFAULT current_timestamp
);

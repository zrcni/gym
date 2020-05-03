CREATE TABLE users (
  user_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  token_user_id varchar(28) NOT NULL,
  username varchar(16) NOT NULL,
  avatar_url text,
  created_at timestamptz NOT NULL DEFAULT current_timestamp,
  modified_at timestamptz NOT NULL DEFAULT current_timestamp
);
--;;
CREATE UNIQUE INDEX "users_token_user_id" on "users" (token_user_id);
--;;
ALTER TABLE workouts ADD COLUMN user_id uuid NOT NULL REFERENCES "users" (user_id);

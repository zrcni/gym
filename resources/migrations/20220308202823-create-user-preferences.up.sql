CREATE TABLE user_preferences (
  user_id uuid PRIMARY KEY REFERENCES "users" (user_id),
  excluded_tags text[] NOT NULL,
  theme_main_color text,
  created_at timestamptz NOT NULL DEFAULT current_timestamp,
  modified_at timestamptz NOT NULL DEFAULT current_timestamp
);
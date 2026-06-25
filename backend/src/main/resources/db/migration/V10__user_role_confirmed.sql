-- Tracks whether the user completed the post-OTP "Who are you?" step.
-- Until confirmed, the app must show role selection even for returning logins.
ALTER TABLE users ADD COLUMN role_confirmed BOOLEAN NOT NULL DEFAULT FALSE;

-- Fully onboarded demo / legacy customers (name + at least one address).
UPDATE users u
SET role_confirmed = TRUE
WHERE u.full_name IS NOT NULL
  AND TRIM(u.full_name) <> ''
  AND EXISTS (
      SELECT 1 FROM customer_addresses ca WHERE ca.user_id = u.id
  );

-- Professionals and admins already chose (or were provisioned with) a role.
UPDATE users SET role_confirmed = TRUE WHERE role IN ('PROFESSIONAL', 'ADMIN');

-- Pros only keep role_confirmed once verification is submitted (PENDING) or approved.
UPDATE users u
SET role_confirmed = FALSE
FROM professional_profiles p
WHERE p.user_id = u.id
  AND u.role = 'PROFESSIONAL'
  AND p.verification_status NOT IN ('PENDING', 'APPROVED');

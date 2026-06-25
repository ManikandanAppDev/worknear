-- The Booking entity has a `noShowAt` field (added with the NO_SHOW status) but the matching
-- column was never created, so any query selecting from `bookings` failed at runtime.
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS no_show_at TIMESTAMPTZ;

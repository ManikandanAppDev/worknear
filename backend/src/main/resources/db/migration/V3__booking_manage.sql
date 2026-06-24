-- Booking manage: reschedule counter, cancellation metadata, late-cancel tracking for fee policy.

ALTER TABLE bookings
    ADD COLUMN reschedule_count     INT NOT NULL DEFAULT 0,
    ADD COLUMN cancellation_comment VARCHAR(500),
    ADD COLUMN late_cancel          BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN cancellation_fee     NUMERIC(12, 2),
    ADD COLUMN cancelled_at         TIMESTAMPTZ;

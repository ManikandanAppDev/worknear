-- Booking status, completion OTP, and wallet lock metadata.

ALTER TABLE bookings
    ADD COLUMN locked_amount             NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN payment_released_at       TIMESTAMPTZ,
    ADD COLUMN completion_otp_code       VARCHAR(12),
    ADD COLUMN completion_otp_hash       VARCHAR(128),
    ADD COLUMN completion_otp_expires_at TIMESTAMPTZ,
    ADD COLUMN on_the_way_at             TIMESTAMPTZ,
    ADD COLUMN arrived_at                TIMESTAMPTZ,
    ADD COLUMN work_started_at           TIMESTAMPTZ,
    ADD COLUMN work_completed_at         TIMESTAMPTZ;

UPDATE bookings
SET locked_amount = amount
WHERE locked_amount = 0
  AND status NOT IN ('CANCELLED', 'REJECTED');

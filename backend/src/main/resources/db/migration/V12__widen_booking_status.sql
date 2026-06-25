-- COMPLETED_PENDING_OTP is 21 characters; status columns were VARCHAR(20).

ALTER TABLE bookings
    ALTER COLUMN status TYPE VARCHAR(32);

ALTER TABLE booking_status_history
    ALTER COLUMN status TYPE VARCHAR(32);

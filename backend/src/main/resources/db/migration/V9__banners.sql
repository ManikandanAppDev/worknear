-- =====================================================================
-- Onboarding banners: admin-managed carousel slides for the app's
-- onboarding screen. Public read via GET /api/v1/banners.
-- =====================================================================

CREATE TABLE banners (
    id          UUID PRIMARY KEY,
    title       VARCHAR(160),
    subtitle    TEXT,
    image_url   VARCHAR(1024),
    cta_label   VARCHAR(80),
    audience    VARCHAR(20)  NOT NULL DEFAULT 'ALL',
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);

-- Default onboarding slides (customer-facing). Replace image URLs from the admin dashboard.
INSERT INTO banners (id, title, subtitle, image_url, cta_label, audience, sort_order, active, created_at, updated_at) VALUES
 ('cccccccc-0000-0000-0000-000000000001', 'Trusted home services', 'Verified professionals, fair prices, on your schedule.', 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=1080&q=80', NULL, 'ALL', 1, TRUE, now(), now()),
 ('cccccccc-0000-0000-0000-000000000002', 'Book in seconds', 'Cleaning, repairs, appliances and more — a tap away.', 'https://images.unsplash.com/photo-1556911220-bff31c812dba?w=1080&q=80', NULL, 'ALL', 2, TRUE, now(), now()),
 ('cccccccc-0000-0000-0000-000000000003', 'Pay only when done', 'Your money is held safely and released after the job.', 'https://images.unsplash.com/photo-1604335399105-a0c585fd81a1?w=1080&q=80', NULL, 'ALL', 3, TRUE, now(), now());

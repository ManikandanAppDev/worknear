-- =====================================================================
-- Seed data: service categories, demo accounts, an offer.
-- Demo phones log in via OTP (mock code 4821).
-- =====================================================================

-- ---------- Service categories ----------
INSERT INTO service_categories (id, slug, name, icon, color, sort_order, active, created_at, updated_at) VALUES
 ('22222222-0000-0000-0000-000000000001', 'electrician', 'Electrician', 'i-bolt',   '#D97706', 1, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000002', 'plumber',     'Plumber',     'i-wrench',  '#2563EB', 2, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000003', 'ac-repair',   'AC Repair',   'i-snow',    '#0EA5A5', 3, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000004', 'painter',     'Painter',     'i-roller',  '#7C3AED', 4, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000005', 'carpenter',   'Carpenter',   'i-saw',     '#B45309', 5, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000006', 'cleaner',     'Cleaner',     'i-broom',   '#15803D', 6, TRUE, now(), now());

-- ---------- Users ----------
INSERT INTO users (id, phone, role, full_name, email, status, created_at, updated_at) VALUES
 ('00000000-0000-0000-0000-000000000001', '+919000000001', 'CUSTOMER',     'Rahul Kumar',  'rahul@example.com',  'ACTIVE', now(), now()),
 ('00000000-0000-0000-0000-000000000002', '+919000000002', 'PROFESSIONAL', 'Ramesh Kumar', 'ramesh@example.com', 'ACTIVE', now(), now()),
 ('00000000-0000-0000-0000-000000000009', '+919000000009', 'ADMIN',        'WorkNear Admin','admin@worknear.com','ACTIVE', now(), now());

-- ---------- Wallets ----------
INSERT INTO wallets (id, user_id, balance, currency, created_at, updated_at) VALUES
 ('33333333-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 2450.00, 'INR', now(), now()),
 ('33333333-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 6840.00, 'INR', now(), now());

-- ---------- Customer address ----------
INSERT INTO customer_addresses (id, user_id, label, line1, city, state, pincode, latitude, longitude, is_default, created_at, updated_at) VALUES
 ('44444444-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'Home', 'Anna Nagar', 'Chennai', 'Tamil Nadu', '600040', 13.0850, 80.2101, TRUE, now(), now());

-- ---------- Professional profile (verified) ----------
INSERT INTO professional_profiles
 (id, user_id, bio, experience_years, service_radius_km, city, area, base_latitude, base_longitude, languages, online, verification_status, rating, rating_count, jobs_completed, created_at, updated_at)
VALUES
 ('55555555-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002',
  'Expert in all types of electrical work - wiring, repairing, installation & maintenance.',
  8, 8, 'Chennai', 'Anna Nagar', 13.0878, 80.2089, 'Tamil, English, Hindi',
  TRUE, 'APPROVED', 4.80, 230, 128, now(), now());

INSERT INTO professional_services (id, professional_id, category_id, base_price, active, created_at, updated_at) VALUES
 ('66666666-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000002', '22222222-0000-0000-0000-000000000001', 299.00, TRUE, now(), now()),
 ('66666666-0000-0000-0000-000000000002', '55555555-0000-0000-0000-000000000002', '22222222-0000-0000-0000-000000000002', 249.00, TRUE, now(), now());

INSERT INTO professional_specializations (id, professional_id, label, created_at, updated_at) VALUES
 ('77777777-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000002', 'House Wiring', now(), now()),
 ('77777777-0000-0000-0000-000000000002', '55555555-0000-0000-0000-000000000002', 'Switchboard',  now(), now()),
 ('77777777-0000-0000-0000-000000000003', '55555555-0000-0000-0000-000000000002', 'Fan Install',  now(), now());

INSERT INTO professional_availability (id, professional_id, day_of_week, start_time, end_time, available, created_at, updated_at) VALUES
 ('88888888-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000002', 1, '09:00', '18:00', TRUE, now(), now()),
 ('88888888-0000-0000-0000-000000000002', '55555555-0000-0000-0000-000000000002', 2, '09:00', '18:00', TRUE, now(), now()),
 ('88888888-0000-0000-0000-000000000003', '55555555-0000-0000-0000-000000000002', 3, '09:00', '18:00', TRUE, now(), now()),
 ('88888888-0000-0000-0000-000000000004', '55555555-0000-0000-0000-000000000002', 4, '09:00', '18:00', TRUE, now(), now()),
 ('88888888-0000-0000-0000-000000000005', '55555555-0000-0000-0000-000000000002', 5, '09:00', '18:00', TRUE, now(), now());

INSERT INTO professional_bank_accounts (id, professional_id, method, upi_id, created_at, updated_at) VALUES
 ('99999999-0000-0000-0000-000000000002', '55555555-0000-0000-0000-000000000002', 'UPI', 'ramesh@okhdfc', now(), now());

INSERT INTO professional_documents (id, professional_id, type, file_url, original_name, status, created_at, updated_at) VALUES
 ('aaaaaaaa-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000002', 'GOV_ID', 'http://localhost:8080/files/seed/aadhaar.pdf', 'Aadhaar.pdf', 'APPROVED', now(), now());

-- ---------- Offer ----------
INSERT INTO offers (id, code, title, description, discount_type, discount_value, max_discount, min_order, active, created_at, updated_at) VALUES
 ('bbbbbbbb-0000-0000-0000-000000000001', 'FIRST20', 'Get 20% OFF', 'On your first booking', 'PERCENT', 20.00, 150.00, 0, TRUE, now(), now());

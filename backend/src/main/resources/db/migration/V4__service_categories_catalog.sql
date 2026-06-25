-- Align service catalog with current WorkNear launch scope.
-- Pest control is intentionally excluded.

INSERT INTO service_categories (id, slug, name, icon, color, sort_order, active, created_at, updated_at) VALUES
 ('22222222-0000-0000-0000-000000000001', 'electrician',                  'Electrician',                  'i-bolt',        '#D97706', 1,  TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000002', 'plumber',                      'Plumber',                      'i-wrench',      '#2563EB', 2,  TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000003', 'ac',                           'AC',                           'i-snow',        '#0EA5A5', 3,  TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000004', 'painting-water-proofing',       'Painting & Water-proofing',    'i-roller',      '#7C3AED', 4,  TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000005', 'carpenter',                    'Carpenter',                    'i-saw',         '#B45309', 5,  TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000006', 'bathroom-cleaning',            'Bathroom Cleaning',            'i-broom',       '#15803D', 6,  TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000007', 'kitchen-cleaning',             'Kitchen Cleaning',             'i-broom',       '#15803D', 7,  TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000008', 'living-bedroom-cleaning',      'Living & Bedroom Cleaning',    'i-broom',       '#15803D', 8,  TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000009', 'full-home-cleaning',           'Full Home/By Room Cleaning',   'i-broom',       '#15803D', 9,  TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000010', 'washing-machine',              'Washing Machine',              'i-appliance',   '#0EA5A5', 10, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000011', 'refrigerator',                 'Refrigerator',                 'i-appliance',   '#0EA5A5', 11, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000012', 'television',                   'Television',                   'i-appliance',   '#0EA5A5', 12, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000013', 'chimney',                      'Chimney',                      'i-appliance',   '#0EA5A5', 13, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000014', 'microwave',                    'Microwave',                    'i-appliance',   '#0EA5A5', 14, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000015', 'stove',                        'Stove',                        'i-appliance',   '#0EA5A5', 15, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000016', 'laptop',                       'Laptop',                       'i-appliance',   '#0EA5A5', 16, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000017', 'ro-water-purifier',            'RO/Water Purifier',            'i-appliance',   '#0EA5A5', 17, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000018', 'geyser',                       'Geyser',                       'i-appliance',   '#0EA5A5', 18, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000019', 'festival-lights-installation', 'Festival Lights Installation', 'i-bolt',        '#D97706', 19, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000020', 'fan-installation',             'Fan Installation',             'i-bolt',        '#D97706', 20, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000021', 'furniture-assembly',           'Furniture Assembly',           'i-saw',         '#B45309', 21, TRUE, now(), now()),
 ('22222222-0000-0000-0000-000000000022', 'geyser-service-repair',        'Geyser Service & Repair',      'i-wrench',      '#2563EB', 22, TRUE, now(), now())
ON CONFLICT (id) DO UPDATE SET
    slug = EXCLUDED.slug,
    name = EXCLUDED.name,
    icon = EXCLUDED.icon,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    active = TRUE,
    updated_at = now();

UPDATE service_categories
SET active = FALSE, updated_at = now()
WHERE slug NOT IN (
    'electrician',
    'plumber',
    'ac',
    'painting-water-proofing',
    'carpenter',
    'bathroom-cleaning',
    'kitchen-cleaning',
    'living-bedroom-cleaning',
    'full-home-cleaning',
    'washing-machine',
    'refrigerator',
    'television',
    'chimney',
    'microwave',
    'stove',
    'laptop',
    'ro-water-purifier',
    'geyser',
    'festival-lights-installation',
    'fan-installation',
    'furniture-assembly',
    'geyser-service-repair'
);

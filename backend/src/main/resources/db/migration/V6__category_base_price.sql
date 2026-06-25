-- Admin-managed reference base price per service category.
-- This is the platform's suggested/starting price shown to customers; individual
-- professionals still set their own price in professional_services.base_price.

ALTER TABLE service_categories
    ADD COLUMN base_price NUMERIC(12, 2) NOT NULL DEFAULT 0;

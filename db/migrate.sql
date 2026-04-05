-- =============================================================================
-- MIT Grand Regency — Migration Script for Existing Installs
-- =============================================================================
-- Run this if you already have the old database schema:
--   mysql -u root -p mit_grand_regency < db/migrate.sql
-- =============================================================================

USE mit_grand_regency;

-- --- rooms table ---
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS checkout_time VARCHAR(30);
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS priority BOOLEAN DEFAULT FALSE;

-- Migrate aadhaar_image (LONGBLOB) → aadhaar_path (VARCHAR)
-- NOTE: Existing blob data will be lost. Re-upload images after migration.
ALTER TABLE rooms DROP COLUMN IF EXISTS aadhaar_image;
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS aadhaar_path VARCHAR(255);

-- --- checkout_history table ---
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS room_type VARCHAR(20);
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS contact_number VARCHAR(20);
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS guest_email VARCHAR(100);
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS guest_address VARCHAR(255);
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS check_in_date VARCHAR(20);
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS price_per_night DOUBLE DEFAULT 0;
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS nights INT DEFAULT 0;
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS subtotal DOUBLE DEFAULT 0;
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS tax_amount DOUBLE DEFAULT 0;
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS gst_rate DOUBLE DEFAULT 18;
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS booked_at DATETIME;

-- Migrate aadhaar_image (LONGBLOB) → aadhaar_path (VARCHAR)
ALTER TABLE checkout_history DROP COLUMN IF EXISTS aadhaar_image;
ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS aadhaar_path VARCHAR(255);

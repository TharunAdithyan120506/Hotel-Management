-- =============================================================================
-- MIT Grand Regency — Fresh Install Schema
-- =============================================================================
-- Run this on a new MariaDB database:
--   mysql -u root -p mit_grand_regency < db/schema.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS mit_grand_regency
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE mit_grand_regency;

-- Application settings (key-value pairs)
CREATE TABLE IF NOT EXISTS settings (
    setting_key   VARCHAR(50)  PRIMARY KEY,
    setting_value VARCHAR(100)
) ENGINE=InnoDB;

-- Room inventory and current occupancy
CREATE TABLE IF NOT EXISTS rooms (
    room_number          VARCHAR(10)  PRIMARY KEY,
    room_type            VARCHAR(20),
    price                DOUBLE,
    status               VARCHAR(30)  DEFAULT 'Available',
    customer_name        VARCHAR(100),
    contact_number       VARCHAR(20),
    guest_email          VARCHAR(100),
    guest_address        VARCHAR(255),
    check_in_date        DATE,
    expected_checkout_date DATE,
    aadhaar_path         VARCHAR(255),
    checkout_time        VARCHAR(30),
    priority             BOOLEAN      DEFAULT FALSE
) ENGINE=InnoDB;

-- Completed checkout records (financial ledger)
CREATE TABLE IF NOT EXISTS checkout_history (
    id               INT          AUTO_INCREMENT PRIMARY KEY,
    room_number      VARCHAR(10),
    room_type        VARCHAR(20),
    guest_name       VARCHAR(100),
    contact_number   VARCHAR(20),
    guest_email      VARCHAR(100),
    guest_address    VARCHAR(255),
    check_in_date    VARCHAR(20),
    checkout_date    VARCHAR(20),
    price_per_night  DOUBLE       DEFAULT 0,
    nights           INT          DEFAULT 0,
    subtotal         DOUBLE       DEFAULT 0,
    tax_amount       DOUBLE       DEFAULT 0,
    gst_rate         DOUBLE       DEFAULT 18,
    total_paid       DOUBLE       DEFAULT 0,
    booked_at        DATETIME,
    aadhaar_path     VARCHAR(255)
) ENGINE=InnoDB;

-- Restaurant menu catalogue
CREATE TABLE IF NOT EXISTS menu_items (
    item_code  VARCHAR(20)  PRIMARY KEY,
    item_name  VARCHAR(100),
    category   VARCHAR(50),
    unit_price DOUBLE
) ENGINE=InnoDB;

-- Active restaurant orders linked to occupied rooms
CREATE TABLE IF NOT EXISTS restaurant_orders (
    id          INT          AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10),
    guest_name  VARCHAR(100),
    item_code   VARCHAR(20),
    item_name   VARCHAR(100),
    category    VARCHAR(50),
    unit_price  DOUBLE,
    quantity    INT,
    total_price DOUBLE,
    order_time  DATETIME,
    settled     BOOLEAN      DEFAULT FALSE
) ENGINE=InnoDB;

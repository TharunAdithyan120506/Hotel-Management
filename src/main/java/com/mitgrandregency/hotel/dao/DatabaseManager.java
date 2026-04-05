package com.mitgrandregency.hotel.dao;

import java.sql.*;

/**
 * Central database connection manager.
 * <p>
 * Reads credentials from {@link ConfigLoader} and provides JDBC connections.
 * Also runs the schema-verification DDL on startup.
 * </p>
 */
public class DatabaseManager {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public DatabaseManager(ConfigLoader config) {
        this.dbUrl = config.getDbUrl();
        this.dbUser = config.getDbUser();
        this.dbPassword = config.getDbPassword();
    }

    /**
     * Returns a new JDBC connection.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Creates all required tables if they don't exist and adds any missing columns.
     * This is safe to run on every startup.
     */
    public void ensureSchema() {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS settings ("
                + "setting_key VARCHAR(50) PRIMARY KEY, setting_value VARCHAR(100))",

            "CREATE TABLE IF NOT EXISTS rooms ("
                + "room_number VARCHAR(10) PRIMARY KEY, room_type VARCHAR(20), price DOUBLE,"
                + "status VARCHAR(30) DEFAULT 'Available', customer_name VARCHAR(100),"
                + "contact_number VARCHAR(20), guest_email VARCHAR(100), guest_address VARCHAR(255),"
                + "check_in_date DATE, expected_checkout_date DATE, aadhaar_path VARCHAR(255),"
                + "checkout_time VARCHAR(30), priority BOOLEAN DEFAULT FALSE)",

            "CREATE TABLE IF NOT EXISTS checkout_history ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, room_number VARCHAR(10), room_type VARCHAR(20),"
                + "guest_name VARCHAR(100), contact_number VARCHAR(20), guest_email VARCHAR(100),"
                + "guest_address VARCHAR(255), check_in_date VARCHAR(20), checkout_date VARCHAR(20),"
                + "price_per_night DOUBLE DEFAULT 0, nights INT DEFAULT 0, subtotal DOUBLE DEFAULT 0,"
                + "tax_amount DOUBLE DEFAULT 0, gst_rate DOUBLE DEFAULT 18, total_paid DOUBLE DEFAULT 0,"
                + "booked_at DATETIME, aadhaar_path VARCHAR(255))",

            "CREATE TABLE IF NOT EXISTS menu_items ("
                + "item_code VARCHAR(20) PRIMARY KEY, item_name VARCHAR(100),"
                + "category VARCHAR(50), unit_price DOUBLE)",

            "CREATE TABLE IF NOT EXISTS restaurant_orders ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, room_number VARCHAR(10), guest_name VARCHAR(100),"
                + "item_code VARCHAR(20), item_name VARCHAR(100), category VARCHAR(50),"
                + "unit_price DOUBLE, quantity INT, total_price DOUBLE,"
                + "order_time DATETIME, settled BOOLEAN DEFAULT FALSE)",

            // Safe ALTER — adds missing columns to existing tables
            "ALTER TABLE rooms ADD COLUMN IF NOT EXISTS checkout_time VARCHAR(30)",
            "ALTER TABLE rooms ADD COLUMN IF NOT EXISTS priority BOOLEAN DEFAULT FALSE",
            "ALTER TABLE rooms ADD COLUMN IF NOT EXISTS aadhaar_path VARCHAR(255)",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS room_type VARCHAR(20)",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS contact_number VARCHAR(20)",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS guest_email VARCHAR(100)",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS guest_address VARCHAR(255)",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS check_in_date VARCHAR(20)",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS price_per_night DOUBLE DEFAULT 0",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS nights INT DEFAULT 0",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS subtotal DOUBLE DEFAULT 0",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS tax_amount DOUBLE DEFAULT 0",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS gst_rate DOUBLE DEFAULT 18",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS booked_at DATETIME",
            "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS aadhaar_path VARCHAR(255)"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    System.err.println("Schema warning (non-fatal): " + e.getMessage());
                }
            }
            System.out.println("[DB] Schema verified OK.");
        } catch (SQLException e) {
            System.err.println("[DB] FATAL: Could not connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

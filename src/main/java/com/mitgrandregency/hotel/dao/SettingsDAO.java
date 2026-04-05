package com.mitgrandregency.hotel.dao;

import com.mitgrandregency.hotel.model.AppState;

import java.sql.*;

/**
 * Data Access Object for the {@code settings} table (DB only).
 * Reads and writes pricing/GST settings to the database.
 * <p>
 * For properties-file configuration, see {@link ConfigLoader}.
 * </p>
 */
public class SettingsDAO {

    private final DatabaseManager db;

    public SettingsDAO(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Loads settings from the database into the given AppState.
     * Only overrides values that exist in the DB — falls back to
     * whatever ConfigLoader already set.
     */
    public void loadSettings(AppState state) {
        String sql = "SELECT setting_key, setting_value FROM settings";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String key = rs.getString("setting_key");
                String value = rs.getString("setting_value");
                if (key == null || value == null) continue;
                try {
                    switch (key) {
                        case "priceSingle" -> state.setPriceSingle(Double.parseDouble(value));
                        case "priceDouble" -> state.setPriceDouble(Double.parseDouble(value));
                        case "priceDeluxe" -> state.setPriceDeluxe(Double.parseDouble(value));
                        case "gstRate"     -> state.setGstRate(Double.parseDouble(value));
                    }
                } catch (NumberFormatException ignored) { }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the current pricing and GST settings to the database.
     */
    public void saveSettings(AppState state) {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE setting_value = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String[][] settings = {
                { "priceSingle", String.valueOf(state.getPriceSingle()) },
                { "priceDouble", String.valueOf(state.getPriceDouble()) },
                { "priceDeluxe", String.valueOf(state.getPriceDeluxe()) },
                { "gstRate",     String.valueOf(state.getGstRate()) }
            };

            for (String[] setting : settings) {
                ps.setString(1, setting[0]);
                ps.setString(2, setting[1]);
                ps.setString(3, setting[1]);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

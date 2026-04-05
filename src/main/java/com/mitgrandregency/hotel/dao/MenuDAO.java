package com.mitgrandregency.hotel.dao;

import com.mitgrandregency.hotel.model.MenuItem;

import java.sql.*;
import java.util.List;

/**
 * Data Access Object for the {@code menu_items} table.
 * Uses {@link PreparedStatement} exclusively.
 */
public class MenuDAO {

    private final DatabaseManager db;

    public MenuDAO(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Loads all menu items from the database.
     */
    public void loadAll(List<MenuItem> target) {
        String sql = "SELECT item_code, item_name, category, unit_price FROM menu_items";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                target.add(new MenuItem(
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getDouble("unit_price")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves all menu items — clears then batch-inserts.
     */
    public void saveAll(List<MenuItem> items) {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement clear = conn.prepareStatement("DELETE FROM menu_items")) {
                clear.executeUpdate();
            }
            String sql = "INSERT INTO menu_items (item_code, item_name, category, unit_price) "
                    + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (MenuItem item : items) {
                    ps.setString(1, item.getItemCode());
                    ps.setString(2, item.getItemName());
                    ps.setString(3, item.getCategory());
                    ps.setDouble(4, item.getUnitPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            System.err.println("MySQL Error saving menu items: " + e.getMessage());
        }
    }
}

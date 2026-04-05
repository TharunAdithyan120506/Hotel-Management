package com.mitgrandregency.hotel.dao;

import com.mitgrandregency.hotel.model.Room;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for the {@code rooms} table.
 * Uses {@link PreparedStatement} exclusively — no string-concatenated SQL.
 */
public class RoomDAO {

    private final DatabaseManager db;

    public RoomDAO(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Loads all rooms from the database.
     */
    public void loadAll(List<Room> target) {
        String sql = "SELECT room_number, room_type, price, status, customer_name, "
                + "contact_number, guest_email, guest_address, check_in_date, "
                + "expected_checkout_date, aadhaar_path, checkout_time, priority FROM rooms";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LocalDate inDate = rs.getDate("check_in_date") != null
                        ? rs.getDate("check_in_date").toLocalDate() : null;
                LocalDate outDate = rs.getDate("expected_checkout_date") != null
                        ? rs.getDate("expected_checkout_date").toLocalDate() : null;

                String checkOutTime = safeGetString(rs, "checkout_time");
                String aadhaarPath = safeGetString(rs, "aadhaar_path");

                Room r = new Room(
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getDouble("price"),
                        rs.getString("status"),
                        rs.getString("customer_name"),
                        rs.getString("contact_number"),
                        rs.getString("guest_email"),
                        rs.getString("guest_address"),
                        inDate, outDate, aadhaarPath, checkOutTime);

                try {
                    r.setPriority(rs.getBoolean("priority"));
                } catch (SQLException ignored) { }

                target.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves all rooms to the database using REPLACE INTO.
     */
    public void saveAll(List<Room> rooms) {
        String sql = "REPLACE INTO rooms (room_number, room_type, price, status, "
                + "customer_name, contact_number, guest_email, guest_address, "
                + "check_in_date, expected_checkout_date, aadhaar_path, "
                + "checkout_time, priority) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Room r : rooms) {
                ps.setString(1, r.getRoomNumber());
                ps.setString(2, r.getRoomType());
                ps.setDouble(3, r.getPrice());
                ps.setString(4, r.getStatus());
                ps.setString(5, r.getCustomerName());
                ps.setString(6, r.getContactNumber());
                ps.setString(7, r.getGuestEmail());
                ps.setString(8, r.getGuestAddress());
                ps.setDate(9, r.getCheckInDate() != null
                        ? Date.valueOf(r.getCheckInDate()) : null);
                ps.setDate(10, r.getExpectedCheckOutDate() != null
                        ? Date.valueOf(r.getExpectedCheckOutDate()) : null);
                ps.setString(11, r.getAadhaarPath());
                ps.setString(12, r.getCheckOutTime());
                ps.setBoolean(13, r.isPriority());
                try {
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println("Could not save room " + r.getRoomNumber() + ": " + ex.getMessage());
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a room from the database by room number.
     */
    public void delete(String roomNumber) {
        String sql = "DELETE FROM rooms WHERE room_number = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String safeGetString(ResultSet rs, String col) {
        try { return rs.getString(col); }
        catch (Exception e) { return null; }
    }
}

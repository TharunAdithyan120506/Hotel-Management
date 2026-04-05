package com.mitgrandregency.hotel.dao;

import com.mitgrandregency.hotel.model.HistoryRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object for the {@code checkout_history} table.
 * Uses {@link PreparedStatement} exclusively.
 */
public class HistoryDAO {

    private final DatabaseManager db;

    public HistoryDAO(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Loads all checkout history records from the database.
     */
    public void loadAll(List<HistoryRecord> target) {
        String sql = "SELECT room_number, room_type, guest_name, contact_number, "
                + "guest_email, guest_address, check_in_date, checkout_date, "
                + "price_per_night, nights, subtotal, tax_amount, gst_rate, "
                + "total_paid, booked_at, aadhaar_path FROM checkout_history";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Timestamp ts = safeGetTimestamp(rs, "booked_at");
                LocalDateTime booked = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                target.add(new HistoryRecord(
                        rs.getString("room_number"),
                        safeGetString(rs, "room_type"),
                        rs.getString("guest_name"),
                        safeGetString(rs, "contact_number"),
                        safeGetString(rs, "guest_email"),
                        safeGetString(rs, "guest_address"),
                        safeGetString(rs, "check_in_date"),
                        safeGetString(rs, "checkout_date"),
                        safeGetDouble(rs, "price_per_night"),
                        safeGetLong(rs, "nights"),
                        safeGetDouble(rs, "subtotal"),
                        safeGetDouble(rs, "tax_amount"),
                        safeGetDouble(rs, "gst_rate"),
                        safeGetDouble(rs, "total_paid"),
                        booked,
                        safeGetString(rs, "aadhaar_path")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves all history records — clears the table first, then re-inserts.
     */
    public void saveAll(List<HistoryRecord> records) {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement clear = conn.prepareStatement("DELETE FROM checkout_history")) {
                clear.executeUpdate();
            }

            String sql = "INSERT INTO checkout_history (room_number, room_type, guest_name, "
                    + "contact_number, guest_email, guest_address, check_in_date, checkout_date, "
                    + "price_per_night, nights, subtotal, tax_amount, gst_rate, total_paid, "
                    + "booked_at, aadhaar_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (HistoryRecord h : records) {
                    ps.setString(1, h.getRoomNumber());
                    ps.setString(2, h.getRoomType());
                    ps.setString(3, h.getGuestName());
                    ps.setString(4, h.getContactNumber());
                    ps.setString(5, h.getGuestEmail());
                    ps.setString(6, h.getGuestAddress());
                    ps.setString(7, h.getCheckInDate());
                    ps.setString(8, h.getCheckOutDate());
                    ps.setDouble(9, h.getPricePerNight());
                    ps.setLong(10, h.getNights());
                    ps.setDouble(11, h.getSubtotal());
                    ps.setDouble(12, h.getTaxAmount());
                    ps.setDouble(13, h.getGstRate());
                    ps.setDouble(14, h.getTotalPaid());
                    ps.setTimestamp(15, h.getBookedAt() != null
                            ? Timestamp.valueOf(h.getBookedAt())
                            : Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(16, h.getAadhaarPath());
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Safe getters for nullable / possibly-missing columns ---

    private String safeGetString(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (Exception e) { return null; }
    }

    private double safeGetDouble(ResultSet rs, String col) {
        try { return rs.getDouble(col); } catch (Exception e) { return 0.0; }
    }

    private long safeGetLong(ResultSet rs, String col) {
        try { return rs.getLong(col); } catch (Exception e) { return 0L; }
    }

    private Timestamp safeGetTimestamp(ResultSet rs, String col) {
        try { return rs.getTimestamp(col); } catch (Exception e) { return null; }
    }
}

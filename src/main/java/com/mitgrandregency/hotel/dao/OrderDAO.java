package com.mitgrandregency.hotel.dao;

import com.mitgrandregency.hotel.model.RestaurantOrder;
import com.mitgrandregency.hotel.model.Room;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object for the {@code restaurant_orders} table.
 * Uses {@link PreparedStatement} exclusively.
 */
public class OrderDAO {

    private final DatabaseManager db;

    public OrderDAO(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Loads orders for currently occupied rooms only.
     *
     * @param rooms  current room list — used to filter out stale orders
     * @param target list to populate
     */
    public void loadAll(List<Room> rooms, List<RestaurantOrder> target) {
        String sql = "SELECT id, room_number, guest_name, item_code, item_name, "
                + "category, unit_price, quantity, total_price, order_time, settled "
                + "FROM restaurant_orders";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String roomNum = rs.getString("room_number");
                boolean isOccupied = rooms.stream()
                        .anyMatch(r -> r.getRoomNumber().equals(roomNum)
                                && "Occupied".equals(r.getStatus()));
                if (!isOccupied) continue;

                Timestamp ts = rs.getTimestamp("order_time");
                LocalDateTime orderTime = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                target.add(new RestaurantOrder(
                        rs.getInt("id"),
                        roomNum,
                        rs.getString("guest_name"),
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getDouble("unit_price"),
                        rs.getInt("quantity"),
                        rs.getDouble("total_price"),
                        orderTime,
                        rs.getBoolean("settled")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves all orders — clears then re-inserts.
     */
    public void saveAll(List<RestaurantOrder> orders) {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement clear = conn.prepareStatement("DELETE FROM restaurant_orders")) {
                clear.executeUpdate();
            }
            String sql = "INSERT INTO restaurant_orders (id, room_number, guest_name, "
                    + "item_code, item_name, category, unit_price, quantity, total_price, "
                    + "order_time, settled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (RestaurantOrder o : orders) {
                    if (o.getId() == 0)
                        ps.setNull(1, Types.INTEGER);
                    else
                        ps.setInt(1, o.getId());
                    ps.setString(2, o.getRoomNumber());
                    ps.setString(3, o.getGuestName());
                    ps.setString(4, o.getItemCode());
                    ps.setString(5, o.getItemName());
                    ps.setString(6, o.getCategory());
                    ps.setDouble(7, o.getUnitPrice());
                    ps.setInt(8, o.getQuantity());
                    ps.setDouble(9, o.getTotalPrice());
                    ps.setTimestamp(10, o.getOrderTime() != null
                            ? Timestamp.valueOf(o.getOrderTime())
                            : Timestamp.valueOf(LocalDateTime.now()));
                    ps.setBoolean(11, o.isSettled());
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

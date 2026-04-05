package com.mitgrandregency.hotel.service;

import com.mitgrandregency.hotel.model.AppState;
import com.mitgrandregency.hotel.model.HistoryRecord;
import com.mitgrandregency.hotel.model.RestaurantOrder;
import com.mitgrandregency.hotel.model.Room;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for room bookings: check-in, check-out, extend stay,
 * and billing calculations.
 */
public class BookingService {

    private final AppState state;

    public BookingService(AppState state) {
        this.state = state;
    }

    /**
     * Checks in a guest to the given room.
     *
     * @param room        the room to book
     * @param name        guest name
     * @param contact     guest contact number
     * @param email       guest email
     * @param address     guest address
     * @param days        number of days
     * @param aadhaarPath relative path to stored Aadhaar image
     */
    public void checkIn(Room room, String name, String contact, String email,
                        String address, int days, String aadhaarPath) {
        room.setStatus("Occupied");
        room.setOccupancy(name, contact, email, address,
                LocalDate.now(), LocalDate.now().plusDays(days), aadhaarPath);
    }

    /**
     * Extends a guest's stay by the specified number of days.
     */
    public void extendStay(Room room, int additionalDays) {
        room.extendStay(additionalDays);
    }

    /**
     * Calculates the checkout billing and creates a HistoryRecord.
     * Also identifies unsettled restaurant orders for this room.
     *
     * @param room the room being checked out
     * @return a CheckoutResult containing the record, settled orders, and totals
     */
    public CheckoutResult prepareCheckout(Room room) {
        long days = ChronoUnit.DAYS.between(room.getCheckInDate(), LocalDate.now());
        if (days == 0) days = 1;

        double subtotal = days * room.getPrice();
        double tax = subtotal * (state.getGstRate() / 100.0);

        double resTotal = 0.0;
        List<RestaurantOrder> settledNow = new ArrayList<>();
        for (RestaurantOrder o : state.getRestaurantOrderList()) {
            if (o.getRoomNumber().equals(room.getRoomNumber()) && !o.isSettled()) {
                resTotal += o.getTotalPrice();
                settledNow.add(o);
            }
        }

        double grandTotal = subtotal + tax + resTotal;

        HistoryRecord record = new HistoryRecord(
                room.getRoomNumber(), room.getRoomType(), room.getCustomerName(),
                room.getContactNumber(), room.getGuestEmail(), room.getGuestAddress(),
                room.getCheckInDate().toString(), LocalDate.now().toString(),
                room.getPrice(), days, subtotal, tax, state.getGstRate(), grandTotal,
                LocalDateTime.now(), room.getAadhaarPath());

        return new CheckoutResult(record, settledNow, resTotal);
    }

    /**
     * Commits the checkout: marks orders settled, adds history, transitions room.
     */
    public void commitCheckout(Room room, CheckoutResult result) {
        for (RestaurantOrder o : result.settledOrders()) {
            o.settledProperty().set(true);
        }
        state.getRestaurantOrderList().removeAll(result.settledOrders());
        state.getHistoryList().add(result.record());
        room.setCheckingOut();
    }

    /**
     * Rollback settled orders on checkout failure.
     */
    public void rollbackCheckout(CheckoutResult result) {
        for (RestaurantOrder o : result.settledOrders()) {
            o.settledProperty().set(false);
            state.getRestaurantOrderList().add(o);
        }
    }

    /**
     * Immutable checkout calculation result.
     */
    public record CheckoutResult(
            HistoryRecord record,
            List<RestaurantOrder> settledOrders,
            double restaurantTotal
    ) {}
}

package com.mitgrandregency.hotel.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Centralized application state container.
 * <p>
 * Holds all shared {@link ObservableList} instances and global settings.
 * Instantiated once in {@code MainApp} and passed via constructor injection
 * to every service and UI view that needs it.
 * </p>
 */
public class AppState {

    private final ObservableList<Room> roomList = FXCollections.observableArrayList(
            r -> new javafx.beans.Observable[]{ r.statusProperty(), r.priorityProperty() }
    );
    private final ObservableList<HistoryRecord> historyList = FXCollections.observableArrayList();
    private final ObservableList<MenuItem> menuItemList = FXCollections.observableArrayList();
    private final ObservableList<RestaurantOrder> restaurantOrderList = FXCollections.observableArrayList();

    private double priceSingle = 1000.0;
    private double priceDouble = 2000.0;
    private double priceDeluxe = 3500.0;
    private double gstRate = 18.0;

    // --- Observable Lists ---

    public ObservableList<Room> getRoomList() {
        return roomList;
    }

    public ObservableList<HistoryRecord> getHistoryList() {
        return historyList;
    }

    public ObservableList<MenuItem> getMenuItemList() {
        return menuItemList;
    }

    public ObservableList<RestaurantOrder> getRestaurantOrderList() {
        return restaurantOrderList;
    }

    // --- Pricing ---

    public double getPriceSingle() { return priceSingle; }
    public void setPriceSingle(double priceSingle) { this.priceSingle = priceSingle; }

    public double getPriceDouble() { return priceDouble; }
    public void setPriceDouble(double priceDouble) { this.priceDouble = priceDouble; }

    public double getPriceDeluxe() { return priceDeluxe; }
    public void setPriceDeluxe(double priceDeluxe) { this.priceDeluxe = priceDeluxe; }

    public double getGstRate() { return gstRate; }
    public void setGstRate(double gstRate) { this.gstRate = gstRate; }

    /**
     * Returns the default price for a given room type based on current settings.
     */
    public double getPriceForType(String roomType) {
        return switch (roomType) {
            case "Single" -> priceSingle;
            case "Double" -> priceDouble;
            case "Deluxe" -> priceDeluxe;
            default -> priceSingle;
        };
    }
}

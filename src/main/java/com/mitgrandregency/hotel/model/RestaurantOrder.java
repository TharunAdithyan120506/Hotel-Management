package com.mitgrandregency.hotel.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Restaurant order model with JavaFX observable properties.
 * Tracks an individual order line item linked to an occupied room.
 */
public class RestaurantOrder {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty roomNumber = new SimpleStringProperty();
    private final StringProperty guestName = new SimpleStringProperty();
    private final StringProperty itemCode = new SimpleStringProperty();
    private final StringProperty itemName = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final DoubleProperty unitPrice = new SimpleDoubleProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final DoubleProperty totalPrice = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDateTime> orderTime = new SimpleObjectProperty<>();
    private final BooleanProperty settled = new SimpleBooleanProperty();

    public RestaurantOrder(int id, String roomNo, String guest, String code,
                           String name, String category, double price, int qty,
                           double total, LocalDateTime time, boolean settled) {
        this.id.set(id);
        this.roomNumber.set(roomNo);
        this.guestName.set(guest);
        this.itemCode.set(code);
        this.itemName.set(name);
        this.category.set(category);
        this.unitPrice.set(price);
        this.quantity.set(qty);
        this.totalPrice.set(total);
        this.orderTime.set(time);
        this.settled.set(settled);
    }

    // --- id ---
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    // --- roomNumber ---
    public String getRoomNumber() { return roomNumber.get(); }
    public StringProperty roomNumberProperty() { return roomNumber; }

    // --- guestName ---
    public String getGuestName() { return guestName.get(); }
    public StringProperty guestNameProperty() { return guestName; }

    // --- itemCode ---
    public String getItemCode() { return itemCode.get(); }
    public StringProperty itemCodeProperty() { return itemCode; }

    // --- itemName ---
    public String getItemName() { return itemName.get(); }
    public StringProperty itemNameProperty() { return itemName; }

    // --- category ---
    public String getCategory() { return category.get(); }
    public StringProperty categoryProperty() { return category; }

    // --- unitPrice ---
    public double getUnitPrice() { return unitPrice.get(); }
    public DoubleProperty unitPriceProperty() { return unitPrice; }

    // --- quantity ---
    public int getQuantity() { return quantity.get(); }
    public IntegerProperty quantityProperty() { return quantity; }

    // --- totalPrice ---
    public double getTotalPrice() { return totalPrice.get(); }
    public DoubleProperty totalPriceProperty() { return totalPrice; }

    // --- orderTime ---
    public LocalDateTime getOrderTime() { return orderTime.get(); }
    public ObjectProperty<LocalDateTime> orderTimeProperty() { return orderTime; }

    // --- settled ---
    public boolean isSettled() { return settled.get(); }
    public BooleanProperty settledProperty() { return settled; }
}

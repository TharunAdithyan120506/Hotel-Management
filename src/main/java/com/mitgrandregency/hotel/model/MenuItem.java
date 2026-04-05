package com.mitgrandregency.hotel.model;

import javafx.beans.property.*;

/**
 * Restaurant menu item model with JavaFX observable properties.
 */
public class MenuItem {

    private final StringProperty itemCode = new SimpleStringProperty();
    private final StringProperty itemName = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final DoubleProperty unitPrice = new SimpleDoubleProperty();

    public MenuItem(String code, String name, String category, double price) {
        this.itemCode.set(code);
        this.itemName.set(name);
        this.category.set(category);
        this.unitPrice.set(price);
    }

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
}

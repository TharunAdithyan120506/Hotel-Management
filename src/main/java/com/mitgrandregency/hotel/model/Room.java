package com.mitgrandregency.hotel.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Room data model with JavaFX observable properties.
 * Represents a single hotel room and its current occupancy state.
 */
public class Room {

    private final StringProperty roomNumber = new SimpleStringProperty();
    private final StringProperty roomType = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty customerName = new SimpleStringProperty();
    private final StringProperty contactNumber = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> checkInDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> expectedCheckOutDate = new SimpleObjectProperty<>();
    private final StringProperty guestEmail = new SimpleStringProperty();
    private final StringProperty guestAddress = new SimpleStringProperty();
    private final StringProperty aadhaarPath = new SimpleStringProperty();
    private final StringProperty checkOutTime = new SimpleStringProperty();
    private final BooleanProperty priority = new SimpleBooleanProperty(false);

    /**
     * Full constructor — used when loading from the database.
     */
    public Room(String no, String t, double p, String s, String c, String pNo,
                String email, String addr, LocalDate in, LocalDate expectedOut,
                String aadhaarPath, String checkoutTime) {
        this.roomNumber.set(no);
        this.roomType.set(t);
        this.price.set(p);
        this.status.set(s);
        this.customerName.set(c);
        this.contactNumber.set(pNo);
        this.guestEmail.set(email);
        this.guestAddress.set(addr);
        this.checkInDate.set(in);
        this.expectedCheckOutDate.set(expectedOut);
        this.aadhaarPath.set(aadhaarPath);
        this.checkOutTime.set(checkoutTime);
    }

    /**
     * Short constructor — used when adding a new empty room to inventory.
     */
    public Room(String no, String t, double p) {
        this(no, t, p, "Available", "", "", "", "", null, null, null, null);
    }

    // --- roomNumber ---
    public String getRoomNumber() { return roomNumber.get(); }
    public StringProperty roomNumberProperty() { return roomNumber; }

    // --- roomType ---
    public String getRoomType() { return roomType.get(); }
    public StringProperty roomTypeProperty() { return roomType; }

    // --- price ---
    public double getPrice() { return price.get(); }
    public DoubleProperty priceProperty() { return price; }

    // --- status ---
    public String getStatus() { return status.get(); }
    public void setStatus(String s) { this.status.set(s); }
    public StringProperty statusProperty() { return status; }

    // --- customerName ---
    public String getCustomerName() { return customerName.get(); }
    public StringProperty customerNameProperty() { return customerName; }

    // --- contactNumber ---
    public String getContactNumber() { return contactNumber.get(); }
    public StringProperty contactNumberProperty() { return contactNumber; }

    // --- checkInDate ---
    public LocalDate getCheckInDate() { return checkInDate.get(); }
    public ObjectProperty<LocalDate> checkInDateProperty() { return checkInDate; }

    // --- expectedCheckOutDate ---
    public LocalDate getExpectedCheckOutDate() { return expectedCheckOutDate.get(); }
    public ObjectProperty<LocalDate> expectedCheckOutDateProperty() { return expectedCheckOutDate; }

    // --- guestEmail ---
    public String getGuestEmail() { return guestEmail.get(); }
    public StringProperty guestEmailProperty() { return guestEmail; }

    // --- guestAddress ---
    public String getGuestAddress() { return guestAddress.get(); }
    public StringProperty guestAddressProperty() { return guestAddress; }

    // --- aadhaarPath ---
    public String getAadhaarPath() { return aadhaarPath.get(); }
    public void setAadhaarPath(String path) { this.aadhaarPath.set(path); }
    public StringProperty aadhaarPathProperty() { return aadhaarPath; }

    // --- checkOutTime ---
    public String getCheckOutTime() { return checkOutTime.get(); }
    public StringProperty checkOutTimeProperty() { return checkOutTime; }

    // --- priority ---
    public boolean isPriority() { return priority.get(); }
    public void setPriority(boolean p) { this.priority.set(p); }
    public BooleanProperty priorityProperty() { return priority; }

    /**
     * Sets full occupancy details during check-in.
     */
    public void setOccupancy(String name, String contact, String email,
                             String addr, LocalDate in, LocalDate out,
                             String aadhaarFilePath) {
        this.customerName.set(name);
        this.contactNumber.set(contact);
        this.guestEmail.set(email);
        this.guestAddress.set(addr);
        this.checkInDate.set(in);
        this.expectedCheckOutDate.set(out);
        this.aadhaarPath.set(aadhaarFilePath);
    }

    /**
     * Extends the stay by adding days to the expected checkout date.
     */
    public void extendStay(int days) {
        if (expectedCheckOutDate.get() != null) {
            expectedCheckOutDate.set(expectedCheckOutDate.get().plusDays(days));
        }
    }

    /**
     * Transitions room to "Cleaning" status after checkout.
     */
    public void setCheckingOut() {
        this.customerName.set("");
        this.contactNumber.set("");
        this.guestEmail.set("");
        this.guestAddress.set("");
        this.checkInDate.set(null);
        this.expectedCheckOutDate.set(null);
        this.aadhaarPath.set(null);
        this.status.set("Cleaning");
        this.checkOutTime.set(LocalDate.now().toString());
    }

    /**
     * Resets room to fully available state.
     */
    public void clearOccupancy() {
        this.customerName.set("");
        this.contactNumber.set("");
        this.guestEmail.set("");
        this.guestAddress.set("");
        this.checkInDate.set(null);
        this.expectedCheckOutDate.set(null);
        this.aadhaarPath.set(null);
        this.status.set("Available");
        this.checkOutTime.set(null);
    }

    /**
     * Serializes room data to comma-separated string for CSV export.
     */
    public String toCSV() {
        return String.join(",",
                getRoomNumber(),
                getRoomType(),
                String.valueOf(getPrice()),
                getStatus(),
                getCustomerName(),
                getContactNumber(),
                checkInDate.get() != null ? checkInDate.get().toString() : "null",
                expectedCheckOutDate.get() != null ? expectedCheckOutDate.get().toString() : "null");
    }
}

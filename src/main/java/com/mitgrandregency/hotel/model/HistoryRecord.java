package com.mitgrandregency.hotel.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Checkout history record model with JavaFX observable properties.
 * Represents a completed guest stay for the financial ledger.
 */
public class HistoryRecord {

    private final StringProperty roomNumber = new SimpleStringProperty();
    private final StringProperty roomType = new SimpleStringProperty();
    private final StringProperty guestName = new SimpleStringProperty();
    private final StringProperty contactNumber = new SimpleStringProperty();
    private final StringProperty guestEmail = new SimpleStringProperty();
    private final StringProperty guestAddress = new SimpleStringProperty();
    private final StringProperty checkInDate = new SimpleStringProperty();
    private final StringProperty checkOutDate = new SimpleStringProperty();
    private final DoubleProperty pricePerNight = new SimpleDoubleProperty();
    private final LongProperty nights = new SimpleLongProperty();
    private final DoubleProperty subtotal = new SimpleDoubleProperty();
    private final DoubleProperty taxAmount = new SimpleDoubleProperty();
    private final DoubleProperty gstRate = new SimpleDoubleProperty();
    private final DoubleProperty totalPaid = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDateTime> bookedAt = new SimpleObjectProperty<>();
    private final StringProperty aadhaarPath = new SimpleStringProperty();
    private final StringProperty paymentMode = new SimpleStringProperty();
    private final StringProperty transactionId = new SimpleStringProperty();

    public HistoryRecord(String roomNo, String type, String guest, String contact,
                         String email, String address, String inDate, String outDate,
                         double price, long nts, double sub, double tax, double gst,
                         double total, LocalDateTime booked, String aadhaarPath) {
        this.roomNumber.set(roomNo);
        this.roomType.set(type);
        this.guestName.set(guest);
        this.contactNumber.set(contact);
        this.guestEmail.set(email);
        this.guestAddress.set(address);
        this.checkInDate.set(inDate);
        this.checkOutDate.set(outDate);
        this.pricePerNight.set(price);
        this.nights.set(nts);
        this.subtotal.set(sub);
        this.taxAmount.set(tax);
        this.gstRate.set(gst);
        this.totalPaid.set(total);
        this.bookedAt.set(booked);
        this.aadhaarPath.set(aadhaarPath);
    }

    // --- roomNumber ---
    public String getRoomNumber() { return roomNumber.get(); }
    public StringProperty roomNumberProperty() { return roomNumber; }

    // --- roomType ---
    public String getRoomType() { return roomType.get(); }
    public StringProperty roomTypeProperty() { return roomType; }

    // --- guestName ---
    public String getGuestName() { return guestName.get(); }
    public StringProperty guestNameProperty() { return guestName; }

    // --- contactNumber ---
    public String getContactNumber() { return contactNumber.get(); }
    public StringProperty contactNumberProperty() { return contactNumber; }

    // --- guestEmail ---
    public String getGuestEmail() { return guestEmail.get(); }
    public StringProperty guestEmailProperty() { return guestEmail; }

    // --- guestAddress ---
    public String getGuestAddress() { return guestAddress.get(); }
    public StringProperty guestAddressProperty() { return guestAddress; }

    // --- checkInDate ---
    public String getCheckInDate() { return checkInDate.get(); }
    public StringProperty checkInDateProperty() { return checkInDate; }

    // --- checkOutDate ---
    public String getCheckOutDate() { return checkOutDate.get(); }
    public StringProperty checkOutDateProperty() { return checkOutDate; }

    // --- pricePerNight ---
    public double getPricePerNight() { return pricePerNight.get(); }
    public DoubleProperty pricePerNightProperty() { return pricePerNight; }

    // --- nights ---
    public long getNights() { return nights.get(); }
    public LongProperty nightsProperty() { return nights; }

    // --- subtotal ---
    public double getSubtotal() { return subtotal.get(); }
    public DoubleProperty subtotalProperty() { return subtotal; }

    // --- taxAmount ---
    public double getTaxAmount() { return taxAmount.get(); }
    public DoubleProperty taxAmountProperty() { return taxAmount; }

    // --- gstRate ---
    public double getGstRate() { return gstRate.get(); }
    public DoubleProperty gstRateProperty() { return gstRate; }

    // --- totalPaid ---
    public double getTotalPaid() { return totalPaid.get(); }
    public DoubleProperty totalPaidProperty() { return totalPaid; }

    // --- bookedAt ---
    public LocalDateTime getBookedAt() { return bookedAt.get(); }
    public ObjectProperty<LocalDateTime> bookedAtProperty() { return bookedAt; }

    // --- aadhaarPath ---
    public String getAadhaarPath() { return aadhaarPath.get(); }
    public void setAadhaarPath(String path) { this.aadhaarPath.set(path); }
    public StringProperty aadhaarPathProperty() { return aadhaarPath; }

    // --- paymentMode ---
    public String getPaymentMode() { return paymentMode.get(); }
    public void setPaymentMode(String mode) { this.paymentMode.set(mode); }
    public StringProperty paymentModeProperty() { return paymentMode; }

    // --- transactionId ---
    public String getTransactionId() { return transactionId.get(); }
    public void setTransactionId(String tid) { this.transactionId.set(tid); }
    public StringProperty transactionIdProperty() { return transactionId; }

    /**
     * Serializes record to comma-separated string for CSV export.
     */
    public String toCSV() {
        return String.join(",",
                getRoomNumber(),
                getRoomType(),
                getGuestName(),
                getContactNumber(),
                getGuestEmail(),
                getGuestAddress() != null ? getGuestAddress().replace(",", " ") : "",
                getCheckInDate(),
                getCheckOutDate(),
                String.valueOf(getNights()),
                String.valueOf(getPricePerNight()),
                String.valueOf(getSubtotal()),
                String.valueOf(getGstRate()),
                String.valueOf(getTaxAmount()),
                String.valueOf(getTotalPaid()),
                getBookedAt() != null
                        ? getBookedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                        : "null",
                getPaymentMode() != null ? getPaymentMode() : "",
                getTransactionId() != null ? getTransactionId() : "");
    }
}

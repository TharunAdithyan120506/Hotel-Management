
/*
-- Run these once in MariaDB before launching the updated app:
ALTER TABLE rooms 
  ADD COLUMN IF NOT EXISTS guest_email VARCHAR(100),
  ADD COLUMN IF NOT EXISTS guest_address VARCHAR(255),
  ADD COLUMN IF NOT EXISTS aadhaar_image LONGBLOB,
  ADD COLUMN IF NOT EXISTS checkout_time VARCHAR(30),
  ADD COLUMN IF NOT EXISTS priority BOOLEAN DEFAULT FALSE;

ALTER TABLE checkout_history
  ADD COLUMN IF NOT EXISTS room_type VARCHAR(20),
  ADD COLUMN IF NOT EXISTS contact_number VARCHAR(20),
  ADD COLUMN IF NOT EXISTS guest_email VARCHAR(100),
  ADD COLUMN IF NOT EXISTS guest_address VARCHAR(255),
  ADD COLUMN IF NOT EXISTS check_in_date VARCHAR(20),
  ADD COLUMN IF NOT EXISTS price_per_night DOUBLE DEFAULT 0,
  ADD COLUMN IF NOT EXISTS nights INT DEFAULT 0,
  ADD COLUMN IF NOT EXISTS subtotal DOUBLE DEFAULT 0,
  ADD COLUMN IF NOT EXISTS tax_amount DOUBLE DEFAULT 0,
  ADD COLUMN IF NOT EXISTS gst_rate DOUBLE DEFAULT 18,
  ADD COLUMN IF NOT EXISTS aadhaar_image LONGBLOB,
  ADD COLUMN IF NOT EXISTS booked_at DATETIME;

CREATE TABLE IF NOT EXISTS menu_items (
  item_code VARCHAR(20) PRIMARY KEY,a
  item_name VARCHAR(100),
  category VARCHAR(50),
  unit_price DOUBLE
);

CREATE TABLE IF NOT EXISTS restaurant_orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  room_number VARCHAR(10),
  guest_name VARCHAR(100),
  item_code VARCHAR(20),
  item_name VARCHAR(100),
  category VARCHAR(50),
  unit_price DOUBLE,
  quantity INT,
  total_price DOUBLE,
  order_time DATETIME,
  settled BOOLEAN DEFAULT FALSE
);
*/
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HotelManagement extends Application {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/mit_grand_regency";
    private static final String DB_USER = "tharun";
    private static final String DB_PASS = "password123";

    // --- Brand Colors ---
    private static final String SIDEBAR_TOP = "#1a1a2e";
    private static final String SIDEBAR_BOTTOM = "#16213e";
    private static final String GOLD_ACCENT = "#c9a96e";
    private static final String GOLD_LIGHT = "#e8d5b5";
    private static final String IVORY_TEXT = "#f0e6d3";
    private static final String BG_WARM = "#faf8f5";
    private static final String CARD_SHADOW = "#00000033";
    private static final String DARK_TEXT = "#2c2c2c";
    private static final String SUCCESS_GREEN = "#2ecc71";
    private static final String DANGER_RED = "#e74c3c";
    private static final String AMBER = "#f39c12";
    private static final String ROYAL_BLUE = "#2980b9";

    // --- Global Settings ---
    private double priceSingle = 1000.0;
    private double priceDouble = 2000.0;
    private double priceDeluxe = 3500.0;
    private double gstRate = 18.0;

    // --- Data Models ---
    public static class Room {

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
        private byte[] aadhaarImage;
        private final StringProperty checkOutTime = new SimpleStringProperty();

        // Used when fetching from DB
        public Room(String no, String t, double p, String s, String c, String pNo, String email, String addr,
                LocalDate in,
                LocalDate expectedOut, byte[] aadhaar, String checkoutTime) {
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
            this.aadhaarImage = aadhaar;
            this.checkOutTime.set(checkoutTime);
        }

        // Used when initializing statically
        public Room(String no, String t, double p) {
            this(no, t, p, "Available", "", "", "", "", null, null, null, null);
        }

        public String getRoomNumber() {
            return roomNumber.get();
        }

        public StringProperty roomNumberProperty() {
            return roomNumber;
        }

        public String getRoomType() {
            return roomType.get();
        }

        public StringProperty roomTypeProperty() {
            return roomType;
        }

        public double getPrice() {
            return price.get();
        }

        public DoubleProperty priceProperty() {
            return price;
        }

        public String getStatus() {
            return status.get();
        }

        public StringProperty statusProperty() {
            return status;
        }

        public String getCustomerName() {
            return customerName.get();
        }

        public StringProperty customerNameProperty() {
            return customerName;
        }

        public String getContactNumber() {
            return contactNumber.get();
        }

        public StringProperty contactNumberProperty() {
            return contactNumber;
        }

        public LocalDate getExpectedCheckOutDate() {
            return expectedCheckOutDate.get();
        }

        public ObjectProperty<LocalDate> expectedCheckOutDateProperty() {
            return expectedCheckOutDate;
        }

        public LocalDate getCheckInDate() {
            return checkInDate.get();
        }

        public ObjectProperty<LocalDate> checkInDateProperty() {
            return checkInDate;
        }

        public String getGuestEmail() {
            return guestEmail.get();
        }

        public StringProperty guestEmailProperty() {
            return guestEmail;
        }

        public String getGuestAddress() {
            return guestAddress.get();
        }

        public StringProperty guestAddressProperty() {
            return guestAddress;
        }

        public void setStatus(String s) {
            this.status.set(s);
        }

        public byte[] getAadhaarImage() {
            return aadhaarImage;
        }

        public void setAadhaarImage(byte[] img) {
            this.aadhaarImage = img;
        }

        public void setOccupancy(String n, String c, String email, String addr, LocalDate in, LocalDate out,
                byte[] aadhaarBytes) {
            this.customerName.set(n);
            this.contactNumber.set(c);
            this.guestEmail.set(email);
            this.guestAddress.set(addr);
            this.checkInDate.set(in);
            this.expectedCheckOutDate.set(out);
            this.aadhaarImage = aadhaarBytes;
        }

        public void extendStay(int days) {
            if (expectedCheckOutDate.get() != null) {
                expectedCheckOutDate.set(expectedCheckOutDate.get().plusDays(days));
            }
        }

        public String getCheckOutTime() {
            return checkOutTime.get();
        }

        public StringProperty checkOutTimeProperty() {
            return checkOutTime;
        }

        public void setCheckingOut() {
            this.customerName.set("");
            this.contactNumber.set("");
            this.guestEmail.set("");
            this.guestAddress.set("");
            this.checkInDate.set(null);
            this.expectedCheckOutDate.set(null);
            this.aadhaarImage = null;
            this.status.set("Cleaning");
            this.checkOutTime.set(LocalDate.now().toString());
        }

        public void clearOccupancy() {
            this.customerName.set("");
            this.contactNumber.set("");
            this.guestEmail.set("");
            this.guestAddress.set("");
            this.checkInDate.set(null);
            this.expectedCheckOutDate.set(null);
            this.aadhaarImage = null;
            this.status.set("Available");
            this.checkOutTime.set(null);
        }

        private final BooleanProperty priority = new SimpleBooleanProperty(false);

        public boolean isPriority() {
            return priority.get();
        }

        public BooleanProperty priorityProperty() {
            return priority;
        }

        public void setPriority(boolean p) {
            this.priority.set(p);
        }

        public String toCSV() {
            return String.join(",", getRoomNumber(), getRoomType(), String.valueOf(getPrice()), getStatus(),
                    getCustomerName(), getContactNumber(),
                    checkInDate.get() != null ? checkInDate.get().toString() : "null",
                    expectedCheckOutDate.get() != null ? expectedCheckOutDate.get().toString() : "null");
        }
    }

    /*
     * ALTER TABLE checkout_history
     * ADD COLUMN room_type VARCHAR(20),
     * ADD COLUMN contact_number VARCHAR(20),
     * ADD COLUMN check_in_date VARCHAR(20),
     * ADD COLUMN price_per_night DOUBLE,
     * ADD COLUMN nights INT,
     * ADD COLUMN subtotal DOUBLE,
     * ADD COLUMN tax_amount DOUBLE,
     * ADD COLUMN gst_rate DOUBLE,
     * ADD COLUMN aadhaar_image LONGBLOB,
     * ADD COLUMN booked_at DATETIME;
     */
    public static class HistoryRecord {

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
        private byte[] aadhaarImage;

        public HistoryRecord(String roomNo, String type, String guest, String contact, String email, String address,
                String inDate, String outDate,
                double price, long nts, double sub, double tax, double gst, double total,
                LocalDateTime booked, byte[] aadhaar) {
            roomNumber.set(roomNo);
            roomType.set(type);
            guestName.set(guest);
            contactNumber.set(contact);
            guestEmail.set(email);
            guestAddress.set(address);
            checkInDate.set(inDate);
            checkOutDate.set(outDate);
            pricePerNight.set(price);
            nights.set(nts);
            subtotal.set(sub);
            taxAmount.set(tax);
            gstRate.set(gst);
            totalPaid.set(total);
            bookedAt.set(booked);
            this.aadhaarImage = aadhaar;
        }

        public String getRoomNumber() {
            return roomNumber.get();
        }

        public StringProperty roomNumberProperty() {
            return roomNumber;
        }

        public String getRoomType() {
            return roomType.get();
        }

        public StringProperty roomTypeProperty() {
            return roomType;
        }

        public String getGuestName() {
            return guestName.get();
        }

        public StringProperty guestNameProperty() {
            return guestName;
        }

        public String getContactNumber() {
            return contactNumber.get();
        }

        public StringProperty contactNumberProperty() {
            return contactNumber;
        }

        public String getGuestEmail() {
            return guestEmail.get();
        }

        public StringProperty guestEmailProperty() {
            return guestEmail;
        }

        public String getGuestAddress() {
            return guestAddress.get();
        }

        public StringProperty guestAddressProperty() {
            return guestAddress;
        }

        public String getCheckInDate() {
            return checkInDate.get();
        }

        public StringProperty checkInDateProperty() {
            return checkInDate;
        }

        public String getCheckOutDate() {
            return checkOutDate.get();
        }

        public StringProperty checkOutDateProperty() {
            return checkOutDate;
        }

        public double getPricePerNight() {
            return pricePerNight.get();
        }

        public DoubleProperty pricePerNightProperty() {
            return pricePerNight;
        }

        public long getNights() {
            return nights.get();
        }

        public LongProperty nightsProperty() {
            return nights;
        }

        public double getSubtotal() {
            return subtotal.get();
        }

        public DoubleProperty subtotalProperty() {
            return subtotal;
        }

        public double getTaxAmount() {
            return taxAmount.get();
        }

        public DoubleProperty taxAmountProperty() {
            return taxAmount;
        }

        public double getGstRate() {
            return gstRate.get();
        }

        public DoubleProperty gstRateProperty() {
            return gstRate;
        }

        public double getTotalPaid() {
            return totalPaid.get();
        }

        public DoubleProperty totalPaidProperty() {
            return totalPaid;
        }

        public LocalDateTime getBookedAt() {
            return bookedAt.get();
        }

        public ObjectProperty<LocalDateTime> bookedAtProperty() {
            return bookedAt;
        }

        public byte[] getAadhaarImage() {
            return aadhaarImage;
        }

        public void setAadhaarImage(byte[] image) {
            this.aadhaarImage = image;
        }

        public String toCSV() {
            return String.join(",", getRoomNumber(), getRoomType(), getGuestName(), getContactNumber(), getGuestEmail(),
                    getGuestAddress().replace(",", " "),
                    getCheckInDate(), getCheckOutDate(), String.valueOf(getNights()),
                    String.valueOf(getPricePerNight()), String.valueOf(getSubtotal()),
                    String.valueOf(getGstRate()), String.valueOf(getTaxAmount()),
                    String.valueOf(getTotalPaid()),
                    getBookedAt() != null ? getBookedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                            : "null");
        }
    }

    public static class MenuItem {
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

        public String getItemCode() {
            return itemCode.get();
        }

        public StringProperty itemCodeProperty() {
            return itemCode;
        }

        public String getItemName() {
            return itemName.get();
        }

        public StringProperty itemNameProperty() {
            return itemName;
        }

        public String getCategory() {
            return category.get();
        }

        public StringProperty categoryProperty() {
            return category;
        }

        public double getUnitPrice() {
            return unitPrice.get();
        }

        public DoubleProperty unitPriceProperty() {
            return unitPrice;
        }
    }

    public static class RestaurantOrder {
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

        public RestaurantOrder(int id, String roomNo, String guest, String code, String name, String category,
                double price, int qty, double total, LocalDateTime time, boolean settled) {
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

        public int getId() {
            return id.get();
        }

        public IntegerProperty idProperty() {
            return id;
        }

        public String getRoomNumber() {
            return roomNumber.get();
        }

        public StringProperty roomNumberProperty() {
            return roomNumber;
        }

        public String getGuestName() {
            return guestName.get();
        }

        public StringProperty guestNameProperty() {
            return guestName;
        }

        public String getItemCode() {
            return itemCode.get();
        }

        public StringProperty itemCodeProperty() {
            return itemCode;
        }

        public String getItemName() {
            return itemName.get();
        }

        public StringProperty itemNameProperty() {
            return itemName;
        }

        public String getCategory() {
            return category.get();
        }

        public StringProperty categoryProperty() {
            return category;
        }

        public double getUnitPrice() {
            return unitPrice.get();
        }

        public DoubleProperty unitPriceProperty() {
            return unitPrice;
        }

        public int getQuantity() {
            return quantity.get();
        }

        public IntegerProperty quantityProperty() {
            return quantity;
        }

        public double getTotalPrice() {
            return totalPrice.get();
        }

        public DoubleProperty totalPriceProperty() {
            return totalPrice;
        }

        public LocalDateTime getOrderTime() {
            return orderTime.get();
        }

        public ObjectProperty<LocalDateTime> orderTimeProperty() {
            return orderTime;
        }

        public boolean isSettled() {
            return settled.get();
        }

        public BooleanProperty settledProperty() {
            return settled;
        }
    }

    // --- State ---
    private final ObservableList<Room> roomList = FXCollections
            .observableArrayList(r -> new javafx.beans.Observable[] { r.statusProperty(), r.priorityProperty() });
    private final ObservableList<HistoryRecord> historyList = FXCollections.observableArrayList();
    private final ObservableList<MenuItem> menuItemList = FXCollections.observableArrayList();
    private final ObservableList<RestaurantOrder> restaurantOrderList = FXCollections.observableArrayList();
    private final StackPane contentArea = new StackPane();

    // Aadhaar State
    private byte[] selectedAadhaarBytes = null;

    // UI Metrics
    private final Label lblTotal = new Label("0"), lblAvail = new Label("0"), lblOcc = new Label("0"),
            lblClean = new Label("0"), lblRevenue = new Label("₹0");
    private VBox revenueCard;

    // Ledger summary label (needs class-level reference for dynamic updates)
    private Label ledgerSummaryLabel;

    // Dashboard activity center (needs class-level reference for dynamic updates)
    private FlowPane activityCenter;

    // Fix 1 & 3: Class level variables
    private javafx.collections.transformation.FilteredList<Room> occupiedRoomsForRestaurant;
    private Runnable refreshHousekeepingPane;

    // Track active nav button
    private Button activeNavButton = null;

    private void ensureSchema() {
        String[] statements = {
                "CREATE TABLE IF NOT EXISTS settings (" +
                        "setting_key VARCHAR(50) PRIMARY KEY, setting_value VARCHAR(100))",

                "CREATE TABLE IF NOT EXISTS rooms (" +
                        "room_number VARCHAR(10) PRIMARY KEY, room_type VARCHAR(20), price DOUBLE," +
                        "status VARCHAR(30) DEFAULT 'Available', customer_name VARCHAR(100)," +
                        "contact_number VARCHAR(20), guest_email VARCHAR(100), guest_address VARCHAR(255)," +
                        "check_in_date DATE, expected_checkout_date DATE, aadhaar_image LONGBLOB," +
                        "checkout_time VARCHAR(30), priority BOOLEAN DEFAULT FALSE)",

                "CREATE TABLE IF NOT EXISTS checkout_history (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, room_number VARCHAR(10), room_type VARCHAR(20)," +
                        "guest_name VARCHAR(100), contact_number VARCHAR(20), guest_email VARCHAR(100)," +
                        "guest_address VARCHAR(255), check_in_date VARCHAR(20), checkout_date VARCHAR(20)," +
                        "price_per_night DOUBLE DEFAULT 0, nights INT DEFAULT 0, subtotal DOUBLE DEFAULT 0," +
                        "tax_amount DOUBLE DEFAULT 0, gst_rate DOUBLE DEFAULT 18, total_paid DOUBLE DEFAULT 0," +
                        "booked_at DATETIME, aadhaar_image LONGBLOB)",

                "CREATE TABLE IF NOT EXISTS menu_items (" +
                        "item_code VARCHAR(20) PRIMARY KEY, item_name VARCHAR(100)," +
                        "category VARCHAR(50), unit_price DOUBLE)",

                "CREATE TABLE IF NOT EXISTS restaurant_orders (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, room_number VARCHAR(10), guest_name VARCHAR(100)," +
                        "item_code VARCHAR(20), item_name VARCHAR(100), category VARCHAR(50)," +
                        "unit_price DOUBLE, quantity INT, total_price DOUBLE," +
                        "order_time DATETIME, settled BOOLEAN DEFAULT FALSE)",

                // Safe ALTER — adds missing columns to existing tables without error
                "ALTER TABLE rooms ADD COLUMN IF NOT EXISTS checkout_time VARCHAR(30)",
                "ALTER TABLE rooms ADD COLUMN IF NOT EXISTS priority BOOLEAN DEFAULT FALSE",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS room_type VARCHAR(20)",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS contact_number VARCHAR(20)",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS guest_email VARCHAR(100)",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS guest_address VARCHAR(255)",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS check_in_date VARCHAR(20)",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS price_per_night DOUBLE DEFAULT 0",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS nights INT DEFAULT 0",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS subtotal DOUBLE DEFAULT 0",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS tax_amount DOUBLE DEFAULT 0",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS gst_rate DOUBLE DEFAULT 18",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS aadhaar_image LONGBLOB",
                "ALTER TABLE checkout_history ADD COLUMN IF NOT EXISTS booked_at DATETIME"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    System.err.println("Schema warning (non-fatal): " + e.getMessage());
                }
            }
            System.out.println("[DB] Schema verified OK.");
        } catch (SQLException e) {
            System.err.println("[DB] FATAL: Could not connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetAllData() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE rooms");
            stmt.execute("TRUNCATE TABLE checkout_history");
            stmt.execute("TRUNCATE TABLE menu_items");
            stmt.execute("TRUNCATE TABLE restaurant_orders");
            stmt.execute("TRUNCATE TABLE settings");

            roomList.clear();
            historyList.clear();
            menuItemList.clear();
            restaurantOrderList.clear();

            loadSettings();
            System.out.println("Clean slate: All tables truncated and memory lists cleared.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        ensureSchema();
        // resetAllData(); // CAUTION: Uncomment to do a clean slate rebuild, then
        // comment out again
        loadSettings();
        loadData();

        // --- Sidebar Navigation ---
        VBox sidebar = new VBox(0);
        sidebar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + SIDEBAR_TOP + ", " + SIDEBAR_BOTTOM + ");");
        sidebar.setPrefWidth(240);

        // Logo area
        VBox logoBox = new VBox(2);
        logoBox.setPadding(new Insets(30, 20, 10, 20));
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("MIT Grand Regency");
        logo.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        logo.setStyle("-fx-text-fill: " + GOLD_ACCENT + ";");

        Label tagline = new Label("Luxury Hospitality Management");
        tagline.setFont(Font.font("Georgia", FontWeight.NORMAL, 11));
        tagline.setStyle("-fx-text-fill: " + IVORY_TEXT + "; -fx-opacity: 0.7;");

        logoBox.getChildren().addAll(logo, tagline);

        // Gold separator line
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setMaxWidth(Double.MAX_VALUE);
        separator.setStyle("-fx-background-color: " + GOLD_ACCENT + "; -fx-opacity: 0.4;");
        VBox.setMargin(separator, new Insets(15, 20, 15, 20));

        StackPane paneDash = createNavButton("\u2302  Dashboard");
        StackPane paneBook = createNavButton("\u270E  Bookings");
        StackPane paneInv = createNavButton("\u2630  Inventory");
        StackPane paneRest = createNavButton("\ud83c\udf7d  Dining & POS");
        StackPane paneHouse = createNavButton("\u2728  Housekeeping");
        StackPane paneLedger = createNavButton("\u2261  Ledger");
        StackPane paneSet = createNavButton("\u2699  Settings");

        Button btnDash = (Button) paneDash.getUserData();
        Button btnBook = (Button) paneBook.getUserData();
        Button btnInv = (Button) paneInv.getUserData();
        Button btnRest = (Button) paneRest.getUserData();
        Button btnHouse = (Button) paneHouse.getUserData();
        Button btnLedger = (Button) paneLedger.getUserData();
        Button btnSet = (Button) paneSet.getUserData();

        VBox navItems = new VBox(4);
        navItems.setPadding(new Insets(0, 10, 20, 10));
        navItems.getChildren().addAll(paneDash, paneBook, paneInv, paneRest, paneHouse, paneLedger, paneSet);

        // Spacer to push footer down
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Sidebar footer
        Label footer = new Label("Tharun Adithyan OSDL Project");
        footer.setStyle("-fx-text-fill: " + IVORY_TEXT + "; -fx-font-size: 10px; -fx-opacity: 0.4;");
        footer.setPadding(new Insets(0, 0, 15, 20));

        sidebar.getChildren().addAll(logoBox, separator, navItems, spacer, footer);

        // --- Content Views ---
        Node viewDash = createDashboardView();
        Node viewBook = createBookingsView();
        Node viewInv = createInventoryView();
        Node viewRest = createRestaurantView();
        Node viewHouse = createHousekeepingView();
        Node viewLedger = createHistoryView();
        Node viewSet = createSettingsView();

        contentArea.setStyle("-fx-background-color: " + BG_WARM + ";");
        contentArea.getChildren().addAll(viewDash, viewBook, viewInv, viewRest, viewHouse, viewLedger, viewSet);
        switchView(viewDash);
        setActiveNav(btnDash);

        // Navigation Logic
        btnDash.setOnAction(e -> {
            switchView(viewDash);
            setActiveNav(btnDash);
            updateDashboardMetrics();
        });
        btnBook.setOnAction(e -> {
            switchView(viewBook);
            setActiveNav(btnBook);
        });
        btnInv.setOnAction(e -> {
            switchView(viewInv);
            setActiveNav(btnInv);
        });
        btnRest.setOnAction(e -> {
            switchView(viewRest);
            setActiveNav(btnRest);
        });
        btnHouse.setOnAction(e -> {
            switchView(viewHouse);
            setActiveNav(btnHouse);
        });
        btnLedger.setOnAction(e -> {
            switchView(viewLedger);
            setActiveNav(btnLedger);
        });
        btnSet.setOnAction(e -> {
            switchView(viewSet);
            setActiveNav(btnSet);
        });

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1250, 750);
        scene.getStylesheets().add(new File("styles.css").toURI().toString());
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;");

        primaryStage.setTitle("MIT Grand Regency — Luxury Hospitality Management");

        WritableImage icon = new WritableImage(64, 64);
        PixelWriter pw = icon.getPixelWriter();
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                pw.setColor(x, y,
                        (x > 20 && x < 44 && y > 20 && y < 44) ? Color.web(GOLD_ACCENT) : Color.web("#1a1a2e"));
            }
        }
        primaryStage.getIcons().add(icon);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(650);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> saveData());

        primaryStage.setOpacity(0);
        primaryStage.show();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(400), new KeyValue(primaryStage.opacityProperty(), 1.0)));
        timeline.play();

        updateDashboardMetrics();
    }

    // --- Styled Navigation Button ---
    private StackPane createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefHeight(42);
        applyNavStyle(btn, false);

        StackPane wrapper = new StackPane(btn);

        TranslateTransition tt = new TranslateTransition(Duration.millis(150), btn);

        wrapper.setOnMouseEntered(e -> {
            if (btn != activeNavButton) {
                tt.stop();
                tt.setFromX(btn.getTranslateX());
                tt.setToX(6);
                tt.play();
                btn.setStyle(
                        "-fx-background-color: rgba(201,169,110,0.15);" +
                                "-fx-text-fill: " + GOLD_ACCENT + ";" +
                                "-fx-font-size: 14px;" +
                                "-fx-alignment: center-left;" +
                                "-fx-padding: 10 20;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;");
            }
        });
        wrapper.setOnMouseExited(e -> {
            if (btn != activeNavButton) {
                tt.stop();
                tt.setFromX(btn.getTranslateX());
                tt.setToX(0);
                tt.play();
                applyNavStyle(btn, false);
            }
        });

        wrapper.setUserData(btn);
        return wrapper;
    }

    private void applyNavStyle(Button btn, boolean active) {
        if (active) {
            btn.setStyle(
                    "-fx-background-color: rgba(201,169,110,0.25);" +
                            "-fx-text-fill: " + GOLD_ACCENT + ";" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-alignment: center-left;" +
                            "-fx-padding: 10 20;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: transparent transparent transparent " + GOLD_ACCENT + ";" +
                            "-fx-border-width: 0 0 0 3;" +
                            "-fx-border-radius: 8;");
        } else {
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: " + IVORY_TEXT + ";" +
                            "-fx-font-size: 14px;" +
                            "-fx-alignment: center-left;" +
                            "-fx-padding: 10 20;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;");
        }
    }

    private void setActiveNav(Button btn) {
        if (activeNavButton != null) {
            applyNavStyle(activeNavButton, false);
            ScaleTransition st1 = new ScaleTransition(Duration.millis(100), (StackPane) activeNavButton.getParent());
            st1.setToX(1.0);
            st1.setToY(1.0);
            st1.play();
        }
        activeNavButton = btn;
        applyNavStyle(btn, true);
        ScaleTransition st2 = new ScaleTransition(Duration.millis(100), (StackPane) btn.getParent());
        st2.setToX(1.02);
        st2.setToY(1.02);
        st2.play();
    }

    private void switchView(Node view) {
        Node outgoing = contentArea.getChildren().stream().filter(Node::isVisible).findFirst().orElse(null);
        if (outgoing == view)
            return;

        if (outgoing != null) {
            FadeTransition ftOut = new FadeTransition(Duration.millis(120), outgoing);
            ftOut.setFromValue(1.0);
            ftOut.setToValue(0.0);

            FadeTransition ftIn = new FadeTransition(Duration.millis(200), view);
            ftIn.setFromValue(0.0);
            ftIn.setToValue(1.0);

            SequentialTransition seq = new SequentialTransition(ftOut, ftIn);
            ftOut.setOnFinished(e -> outgoing.setVisible(false));

            view.setOpacity(0.0);
            view.setVisible(true);
            seq.play();
        } else {
            for (Node n : contentArea.getChildren()) {
                n.setVisible(false);
            }
            view.setOpacity(1.0);
            view.setVisible(true);
        }
    }

    // --- Section Header ---
    private VBox createSectionHeader(String title, String subtitle) {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 10, 0));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
        titleLabel.setStyle("-fx-text-fill: " + DARK_TEXT + ";");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subtitleLabel.setStyle("-fx-text-fill: #888888;");

        // Gold underline
        Region underline = new Region();
        underline.setPrefHeight(3);
        underline.setPrefWidth(60);
        underline.setMaxWidth(60);
        underline.setStyle("-fx-background-color: " + GOLD_ACCENT + "; -fx-background-radius: 2;");

        header.getChildren().addAll(titleLabel, subtitleLabel, underline);
        return header;
    }

    // --- Dashboard Card (brand-consistent palette) ---
    private VBox createCard(String title, Label val, String gradientStart, String gradientEnd, String icon) {
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 38px;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        val.setStyle("-fx-text-fill: white; -fx-font-size: 34px; -fx-font-weight: bold;");

        VBox textBox = new VBox(4, titleLbl, val);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox content = new HBox(12, textBox, iconLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        VBox card = new VBox(content);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setPrefHeight(110);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + gradientStart + ", " + gradientEnd + ");" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0.0, 0, 4);");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // --- Bento-box tile for all rooms ---
    private VBox createRoomTile(Room room) {
        Label roomNo = new Label("Room " + room.getRoomNumber());
        roomNo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");

        Label roomType = new Label(room.getRoomType());
        roomType.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GOLD_ACCENT + "; -fx-font-weight: bold;");

        Region topAccent = new Region();
        topAccent.setPrefHeight(3);
        topAccent.setMaxWidth(Double.MAX_VALUE);

        VBox tile = new VBox(5);

        if ("Available".equals(room.getStatus())) {
            topAccent.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 3 3 0 0;");
            Label rateLabel = new Label("Rate: \u20B9" + room.getPrice() + "/Day");
            rateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            Label guestLabel = new Label("\u263A \u2014");
            guestLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa;");
            Label availBadge = new Label("Available");
            availBadge.setStyle(
                    "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 10;");

            tile.getChildren().addAll(topAccent, roomNo, roomType, rateLabel, guestLabel, availBadge);

        } else if ("Occupied".equals(room.getStatus())) {
            topAccent.setStyle("-fx-background-color: " + GOLD_ACCENT + "; -fx-background-radius: 3 3 0 0;");
            Label guestLabel = new Label("\u263A " + room.getCustomerName());
            guestLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            Label contactLabel = new Label("\u260E " + room.getContactNumber());
            contactLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

            String checkoutStr = room.getExpectedCheckOutDate() != null ? room.getExpectedCheckOutDate().toString()
                    : "N/A";
            Label checkoutLabel = new Label("Checkout: " + checkoutStr);
            checkoutLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

            String daysText = "";
            if (room.getExpectedCheckOutDate() != null) {
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), room.getExpectedCheckOutDate());
                if (daysLeft <= 0) {
                    daysText = "OVERDUE";
                    checkoutLabel
                            .setStyle("-fx-font-size: 11px; -fx-text-fill: " + DANGER_RED + "; -fx-font-weight: bold;");
                } else if (daysLeft == 1) {
                    daysText = "1 day left";
                } else {
                    daysText = daysLeft + " days left";
                }
            }
            Label daysLabel = new Label(daysText);
            daysLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + GOLD_ACCENT + "; -fx-font-weight: bold;");

            tile.getChildren().addAll(topAccent, roomNo, roomType, guestLabel, contactLabel, checkoutLabel, daysLabel);

            if (room.getAadhaarImage() != null) {
                ImageView aadhaarThumb = new ImageView(new Image(new ByteArrayInputStream(room.getAadhaarImage())));
                aadhaarThumb.setFitWidth(60);
                aadhaarThumb.setFitHeight(40);
                aadhaarThumb.setPreserveRatio(true);
                Label checkLabel = new Label("ID on file \u2713");
                checkLabel.setStyle("-fx-text-fill: green; -fx-font-size: 10px;");
                HBox thumbBox = new HBox(5, aadhaarThumb, checkLabel);
                thumbBox.setAlignment(Pos.CENTER_LEFT);
                tile.getChildren().add(thumbBox);
            }

        } else if ("Cleaning".equals(room.getStatus())) {
            topAccent.setStyle("-fx-background-color: #f39c12; -fx-background-radius: 3 3 0 0;");
            Label cleanLabel = new Label("Being Cleaned");
            cleanLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
            tile.getChildren().addAll(topAccent, roomNo, roomType, cleanLabel);

        } else if ("Urgent Cleaning".equals(room.getStatus())) {
            topAccent.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 3 3 0 0;");
            Label urgentLabel = new Label("⚠ URGENT");
            urgentLabel.setStyle(
                    "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 10;");
            tile.getChildren().addAll(topAccent, roomNo, roomType, urgentLabel);
        } else {
            topAccent.setStyle("-fx-background-color: #aaaaaa; -fx-background-radius: 3 3 0 0;");
            tile.getChildren().addAll(topAccent, roomNo, roomType);
        }

        tile.setPadding(new Insets(0, 14, 12, 14));
        tile.setPrefWidth(195);
        tile.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0.0, 0, 3);");
        return tile;
    }

    // --- Refresh Activity Center ---
    private void refreshActivityCenter(String filter) {
        if (activityCenter == null)
            return;
        activityCenter.getChildren().clear();

        List<Room> filteredRooms;
        if ("All".equals(filter)) {
            filteredRooms = new ArrayList<>(roomList);
        } else if ("Urgent".equals(filter)) {
            filteredRooms = roomList.stream().filter(r -> "Urgent Cleaning".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            filteredRooms = roomList.stream().filter(r -> filter.equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (filteredRooms.isEmpty()) {
            Label empty = new Label("No rooms to display.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa; -fx-padding: 30;");
            activityCenter.getChildren().add(empty);
        } else {
            int i = 0;
            for (Room r : filteredRooms) {
                VBox tile = createRoomTile(r);
                activityCenter.getChildren().add(tile);

                FadeTransition ft = new FadeTransition(Duration.millis(200), tile);
                ft.setFromValue(0);
                ft.setToValue(1);
                TranslateTransition tt = new TranslateTransition(Duration.millis(200), tile);
                tt.setFromY(15);
                tt.setToY(0);

                ParallelTransition pt = new ParallelTransition(ft, tt);
                PauseTransition pause = new PauseTransition(Duration.millis(30 * i));
                SequentialTransition st = new SequentialTransition(pause, pt);
                st.play();
                i++;
            }
        }
    }

    // --- View Generators ---
    private Node createDashboardView() {
        // Stats row — brand-consistent dark navy palette
        revenueCard = createCard("Revenue", lblRevenue, "#2a1a3c", "#3d2452", "\u2605");
        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER_LEFT);
        cards.getChildren().addAll(
                createCard("Total Rooms", lblTotal, "#1a1a2e", "#16213e", "\u2302"),
                createCard("Available", lblAvail, "#1a3c34", "#1e5245", "\u2714"),
                createCard("Occupied", lblOcc, "#3c1a1a", "#522020", "\u263A"),
                createCard("Cleaning", lblClean, "#1a2a3c", "#1e3048", "\u2728"),
                revenueCard);

        // Activity Center header
        Label actHeader = new Label("Activity Center — Live Occupancy");
        actHeader.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        actHeader.setStyle("-fx-text-fill: " + DARK_TEXT + ";");

        Label actSub = new Label("Rooms currently in use with guest details");
        actSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");

        Region actUnderline = new Region();
        actUnderline.setPrefHeight(2);
        actUnderline.setPrefWidth(50);
        actUnderline.setMaxWidth(50);
        actUnderline.setStyle("-fx-background-color: " + GOLD_ACCENT + "; -fx-background-radius: 2;");

        VBox actHeaderBox = new VBox(4, actHeader, actSub, actUnderline);

        // Filter Bar
        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup filterGroup = new ToggleGroup();
        String[] filters = { "All", "Available", "Occupied", "Cleaning", "Urgent" };
        for (String f : filters) {
            ToggleButton tb = new ToggleButton(f);
            tb.setToggleGroup(filterGroup);
            tb.setStyle(
                    "-fx-background-color: transparent; -fx-border-color: #ccc; -fx-border-radius: 15; -fx-background-radius: 15; -fx-text-fill: #555; -fx-font-weight: bold; -fx-padding: 4 12;");

            tb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    tb.setStyle("-fx-background-color: " + GOLD_ACCENT + "; -fx-border-color: " + GOLD_ACCENT
                            + "; -fx-border-radius: 15; -fx-background-radius: 15; -fx-text-fill: " + DARK_TEXT
                            + "; -fx-font-weight: bold; -fx-padding: 4 12;");
                    refreshActivityCenter(f);
                } else {
                    tb.setStyle(
                            "-fx-background-color: transparent; -fx-border-color: #ccc; -fx-border-radius: 15; -fx-background-radius: 15; -fx-text-fill: #555; -fx-font-weight: bold; -fx-padding: 4 12;");
                }
            });
            if ("All".equals(f))
                tb.setSelected(true);
            filterBar.getChildren().add(tb);
        }

        filterGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null)
                oldToggle.setSelected(true);
        });

        VBox actHeaderAndFilterBox = new VBox(10, actHeaderBox, filterBar);

        // Bento-box FlowPane
        activityCenter = new FlowPane();
        activityCenter.setHgap(14);
        activityCenter.setVgap(14);
        activityCenter.setPadding(new Insets(5));
        refreshActivityCenter("All");

        ScrollPane scrollPane = new ScrollPane(activityCenter);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox activityCard = new VBox(12, actHeaderAndFilterBox, scrollPane);
        activityCard.setPadding(new Insets(20));
        activityCard.setStyle(
                "-fx-background-color: #f5f2ee;" +
                        "-fx-background-radius: 14;");
        VBox.setVgrow(activityCard, Priority.ALWAYS);

        VBox root = new VBox(22,
                createSectionHeader("Live Overview", "Real-time hotel occupancy and revenue at a glance"),
                cards,
                activityCard);
        root.setPadding(new Insets(30));
        VBox.setVgrow(activityCard, Priority.ALWAYS);

        root.visibleProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                SequentialTransition st = new SequentialTransition();
                for (Node c : cards.getChildren()) {
                    c.setOpacity(0);
                    TranslateTransition tt = new TranslateTransition(Duration.millis(300), c);
                    tt.setFromY(30);
                    tt.setToY(0);
                    FadeTransition ft = new FadeTransition(Duration.millis(300), c);
                    ft.setFromValue(0.0);
                    ft.setToValue(1.0);
                    ParallelTransition pt = new ParallelTransition(tt, ft);
                    st.getChildren().add(pt);
                    st.getChildren().add(new PauseTransition(Duration.millis(60)));
                }
                st.play();
            }
        });

        return root;
    }

    private void updateDashboardMetrics() {
        lblTotal.setText(String.valueOf(roomList.size()));
        lblAvail.setText(String.valueOf(roomList.stream().filter(r -> r.getStatus().equals("Available")).count()));
        lblOcc.setText(String.valueOf(roomList.stream().filter(r -> r.getStatus().equals("Occupied")).count()));
        lblClean.setText(String.valueOf(roomList.stream().filter(r -> r.getStatus().equals("Cleaning")).count()));
        double totalRevenue = historyList.stream().mapToDouble(HistoryRecord::getTotalPaid).sum();
        lblRevenue.setText(String.format("\u20B9%.0f", totalRevenue));

        // Refresh bento-box activity center
        refreshActivityCenter("All");

        // Update ledger summary bar
        updateLedgerSummary();

        // Ensure real-time RESTAURANT drop-down updates
        if (occupiedRoomsForRestaurant != null) {
            occupiedRoomsForRestaurant.setPredicate(null);
            occupiedRoomsForRestaurant.setPredicate(r -> "Occupied".equals(r.getStatus()));
        }

        // Live Housekeeping resort updates
        if (refreshHousekeepingPane != null)
            refreshHousekeepingPane.run();
    }

    private void updateLedgerSummary() {
        if (ledgerSummaryLabel != null) {
            double totalRev = historyList.stream().mapToDouble(HistoryRecord::getTotalPaid).sum();
            ledgerSummaryLabel.setText(
                    String.format("Total Revenue: \u20B9%.2f  |  Total Checkouts: %d", totalRev, historyList.size()));
        }
    }

    // --- Styled Table ---
    @SuppressWarnings("unchecked")
    private Node createBookingsView() {
        java.util.Map<Room, Boolean> recentlyBooked = new java.util.HashMap<>();

        TableView<Room> table = new TableView<>();
        table.setRowFactory(tv -> {
            TableRow<Room> row = new TableRow<Room>() {
                @Override
                protected void updateItem(Room item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        getStyleClass().remove("row-success");
                    } else if (recentlyBooked.getOrDefault(item, false)) {
                        if (!getStyleClass().contains("row-success"))
                            getStyleClass().add("row-success");
                        PauseTransition pt = new PauseTransition(Duration.millis(1500));
                        pt.setOnFinished(evt -> {
                            getStyleClass().remove("row-success");
                            recentlyBooked.put(item, false);
                        });
                        pt.play();
                    }
                }
            };

            ScaleTransition stHover = new ScaleTransition(Duration.millis(100), row);
            stHover.setToX(1.005);
            stHover.setToY(1.005);
            ScaleTransition stExit = new ScaleTransition(Duration.millis(100), row);
            stExit.setToX(1.0);
            stExit.setToY(1.0);

            row.setOnMouseEntered(e -> {
                if (!row.isEmpty())
                    stHover.playFromStart();
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty())
                    stExit.playFromStart();
            });
            return row;
        });
        table.getStyleClass().add("table-view");
        table.setFixedCellSize(40);

        FilteredList<Room> filteredData = new FilteredList<>(roomList, p -> true);
        table.setItems(filteredData);

        TableColumn<Room, String> colRoom = new TableColumn<>("Room #");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoom.setStyle("-fx-font-weight: bold;");

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<Room, String> colStat = new TableColumn<>("Status");
        colStat.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStat.setCellFactory(c -> new TableCell<Room, String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setPadding(new Insets(3, 12, 3, 12));
                    if (item.equals("Available")) {
                        badge.setStyle(
                                "-fx-background-color: #e8f8f0; -fx-text-fill: #27ae60;" +
                                        "-fx-font-weight: bold; -fx-font-size: 11px;" +
                                        "-fx-background-radius: 12;");
                    } else if (item.equals("Cleaning")) {
                        badge.setStyle(
                                "-fx-background-color: #fff3cd; -fx-text-fill: #856404;" +
                                        "-fx-font-weight: bold; -fx-font-size: 11px;" +
                                        "-fx-background-radius: 12;");
                    } else {
                        badge.setStyle(
                                "-fx-background-color: #fdecea; -fx-text-fill: #c0392b;" +
                                        "-fx-font-weight: bold; -fx-font-size: 11px;" +
                                        "-fx-background-radius: 12;");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Room, String> colGuest = new TableColumn<>("Guest Name");
        colGuest.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<Room, String> colContact = new TableColumn<>("Contact");
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        TableColumn<Room, LocalDate> colIn = new TableColumn<>("Check-In");
        colIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));

        TableColumn<Room, LocalDate> colOut = new TableColumn<>("Check-Out");
        colOut.setCellValueFactory(new PropertyValueFactory<>("expectedCheckOutDate"));

        TableColumn<Room, Boolean> colPriority = new TableColumn<>("\u2691");
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colPriority.setCellFactory(c -> new TableCell<Room, Boolean>() {
            @Override
            protected void updateItem(Boolean val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null || !val) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label("URGENT");
                    badge.setStyle(
                            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 8;");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colPriority.setPrefWidth(70);
        colPriority.setMaxWidth(70);

        table.getColumns().addAll(colRoom, colType, colStat, colGuest, colContact, colIn, colOut, colPriority);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Filter bar
        ComboBox<String> filter = new ComboBox<>(
                FXCollections.observableArrayList("All Rooms", "Available", "Occupied"));
        filter.setValue("All Rooms");
        filter.setStyle("-fx-font-size: 13px; -fx-pref-width: 160;");
        filter.setOnAction(e -> filteredData
                .setPredicate(r -> filter.getValue().equals("All Rooms") || r.getStatus().equals(filter.getValue())));

        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        HBox filterBar = new HBox(10, filterLabel, filter);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Action Panel
        TextField txtCust = new TextField();
        txtCust.setPromptText("Guest Name");
        styleTextField(txtCust);

        TextField txtCont = new TextField();
        txtCont.setPromptText("Contact Number");
        styleTextField(txtCont);

        Spinner<Integer> spinDays = new Spinner<>(1, 30, 1);
        spinDays.setPrefWidth(100);
        spinDays.setStyle("-fx-font-size: 13px;");

        Button btnBook = new Button("✔ Book Room");
        btnBook.setStyle(
                "-fx-background-color: #c9a96e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-font-size: 13px; -fx-background-radius: 6;");
        btnBook.setMaxWidth(Double.MAX_VALUE);

        Button btnOut = new Button("✗ Checkout");
        btnOut.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-font-size: 13px; -fx-background-radius: 6;");
        btnOut.setMaxWidth(Double.MAX_VALUE);

        Button btnExt = new Button("⏱ Extend Stay");
        btnExt.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-font-size: 13px; -fx-background-radius: 6;");
        btnExt.setMaxWidth(Double.MAX_VALUE);

        Button btnUrgent = new Button("⚠ Mark Urgent");
        btnUrgent.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-font-size: 13px; -fx-background-radius: 6;");
        btnUrgent.setMaxWidth(Double.MAX_VALUE);

        btnUrgent.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r != null) {
                r.setStatus("Urgent Cleaning");
                table.refresh();
                updateDashboardMetrics();
                saveData();
                btnUrgent.setText("⚠ Already Urgent");
                btnUrgent.setDisable(true);
            }
        });

        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(12);
        grid.setPadding(new Insets(15));

        Label lName = new Label("Guest Name");
        lName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        Label lPhone = new Label("Phone");
        lPhone.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        Label lEmail = new Label("Email Address");
        lEmail.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        Label lAddress = new Label("Home Address");
        lAddress.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        Label lDays = new Label("Duration (Days)");
        lDays.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        Label lAadhaar = new Label("Aadhaar Card Image:");
        lAadhaar.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");

        ImageView aadhaarPreview = new ImageView();
        aadhaarPreview.setFitWidth(150);
        aadhaarPreview.setFitHeight(100);
        aadhaarPreview.setPreserveRatio(true);
        VBox previewBox = new VBox(aadhaarPreview);
        previewBox.setAlignment(Pos.CENTER);
        previewBox.setPrefSize(150, 100);
        previewBox.setStyle("-fx-border-color: #aaa; -fx-border-style: dashed; -fx-background-color: #f9f9f9;");

        Button btnUpload = new Button("\ud83d\udcc1 Upload Image");
        btnUpload.setStyle("-fx-font-size: 10px;");
        Button btnWebcam = new Button("\ud83d\udcf7 Capture");
        btnWebcam.setStyle("-fx-font-size: 10px;");
        btnWebcam.setDisable(true);
        btnWebcam.setTooltip(new Tooltip("Webcam capture requires external hardware"));

        btnUpload.setOnAction(evt -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fc.showOpenDialog(btnUpload.getScene().getWindow());
            if (file != null) {
                if (file.length() > 5 * 1024 * 1024) {
                    showAlert(Alert.AlertType.WARNING, "File Too Large", "Aadhaar image must be under 5MB.");
                    return;
                }
                try {
                    selectedAadhaarBytes = Files.readAllBytes(file.toPath());
                    aadhaarPreview.setImage(new Image(new ByteArrayInputStream(selectedAadhaarBytes)));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        HBox uploadRow = new HBox(5, btnUpload, btnWebcam);

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email Address");
        styleTextField(txtEmail);

        TextField txtAddress = new TextField();
        txtAddress.setPromptText("Home Address");
        styleTextField(txtAddress);

        grid.add(lName, 0, 0);
        grid.add(txtCust, 0, 1);
        grid.add(lPhone, 0, 2);
        grid.add(txtCont, 0, 3);
        grid.add(lEmail, 0, 4);
        grid.add(txtEmail, 0, 5);
        grid.add(lAddress, 0, 6);
        grid.add(txtAddress, 0, 7);
        grid.add(lAadhaar, 0, 8);
        grid.add(previewBox, 0, 9);
        grid.add(uploadRow, 0, 10);
        grid.add(lDays, 0, 11);
        grid.add(spinDays, 0, 12);

        VBox btnRow = new VBox(10, btnBook, btnExt, btnOut, btnUrgent);
        btnRow.setPadding(new Insets(8, 0, 0, 0));
        grid.add(btnRow, 0, 13);

        // Wrap action panel in styled card
        Label actionTitle = new Label("Action Desk");
        actionTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        actionTitle.setStyle("-fx-text-fill: " + DARK_TEXT + ";");

        Region actionSep = new Region();
        actionSep.setPrefHeight(2);
        actionSep.setMaxWidth(Double.MAX_VALUE);
        actionSep.setStyle("-fx-background-color: " + GOLD_ACCENT + "; -fx-opacity: 0.5;");

        ScrollPane actionScroll = new ScrollPane(grid);
        actionScroll.setFitToWidth(true);
        actionScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        actionScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        actionScroll.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        actionScroll.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(actionScroll, Priority.ALWAYS);

        VBox actionCard = new VBox(10, actionTitle, actionSep, actionScroll);
        actionCard.setPadding(new Insets(20));
        actionCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.0, 0, 3);");
        actionCard.setPrefWidth(320);
        actionCard.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(actionCard, Priority.ALWAYS);

        Consumer<Room> updateActionForm = sel -> {
            boolean noRoom = (sel == null);
            btnBook.setVisible(false);
            btnBook.setManaged(false);
            btnExt.setVisible(false);
            btnExt.setManaged(false);
            btnOut.setVisible(false);
            btnOut.setManaged(false);
            btnUrgent.setVisible(false);
            btnUrgent.setManaged(false);

            if (noRoom) {
                txtCust.clear();
                txtCont.clear();
                txtEmail.clear();
                txtAddress.clear();
                aadhaarPreview.setImage(null);
                selectedAadhaarBytes = null;
                return;
            }

            if ("Available".equals(sel.getStatus())) {
                btnBook.setVisible(true);
                btnBook.setManaged(true);
                txtCust.clear();
                txtCont.clear();
                txtEmail.clear();
                txtAddress.clear();
                aadhaarPreview.setImage(null);
                selectedAadhaarBytes = null;
                txtCust.setEditable(true);
                txtCont.setEditable(true);
                txtEmail.setEditable(true);
                txtAddress.setEditable(true);
                spinDays.setDisable(false);
                btnUpload.setDisable(false);

            } else if ("Occupied".equals(sel.getStatus())) {
                btnExt.setVisible(true);
                btnExt.setManaged(true);
                btnOut.setVisible(true);
                btnOut.setManaged(true);

                txtCust.setText(sel.getCustomerName() != null ? sel.getCustomerName() : "");
                txtCont.setText(sel.getContactNumber() != null ? sel.getContactNumber() : "");
                txtEmail.setText(sel.getGuestEmail() != null ? sel.getGuestEmail() : "");
                txtAddress.setText(sel.getGuestAddress() != null ? sel.getGuestAddress() : "");
                if (sel.getAadhaarImage() != null) {
                    aadhaarPreview.setImage(new Image(new ByteArrayInputStream(sel.getAadhaarImage())));
                    selectedAadhaarBytes = sel.getAadhaarImage();
                } else {
                    aadhaarPreview.setImage(null);
                    selectedAadhaarBytes = null;
                }
                txtCust.setEditable(false);
                txtCont.setEditable(false);
                txtEmail.setEditable(false);
                txtAddress.setEditable(false);
                spinDays.setDisable(true);
                btnUpload.setDisable(true);

            } else if ("Cleaning".equals(sel.getStatus()) || "Urgent Cleaning".equals(sel.getStatus())) {
                btnUrgent.setVisible(true);
                btnUrgent.setManaged(true);
                if ("Urgent Cleaning".equals(sel.getStatus())) {
                    btnUrgent.setText("⚠ Already Urgent");
                    btnUrgent.setDisable(true);
                } else {
                    btnUrgent.setText("⚠ Mark Urgent");
                    btnUrgent.setDisable(false);
                }

                txtCust.clear();
                txtCont.clear();
                txtEmail.clear();
                txtAddress.clear();
                aadhaarPreview.setImage(null);
                selectedAadhaarBytes = null;
            } else {
                txtCust.clear();
                txtCont.clear();
                txtEmail.clear();
                txtAddress.clear();
                aadhaarPreview.setImage(null);
                selectedAadhaarBytes = null;
            }
        };
        // Set initial state — all hidden until a room is selected
        btnBook.setVisible(false);
        btnBook.setManaged(false);
        btnExt.setVisible(false);
        btnExt.setManaged(false);
        btnOut.setVisible(false);
        btnOut.setManaged(false);
        btnUrgent.setVisible(false);
        btnUrgent.setManaged(false);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            updateActionForm.accept(sel);
        });

        btnBook.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r != null && !txtCust.getText().isEmpty()) {
                if (selectedAadhaarBytes == null) {
                    showAlert(Alert.AlertType.WARNING, "Missing Image", "Aadhaar card image is required for check-in.");
                    return;
                }
                r.setStatus("Occupied");
                r.setOccupancy(txtCust.getText(), txtCont.getText(), txtEmail.getText(), txtAddress.getText(),
                        LocalDate.now(),
                        LocalDate.now().plusDays(spinDays.getValue()), selectedAadhaarBytes);

                selectedAadhaarBytes = null;
                aadhaarPreview.setImage(null);

                recentlyBooked.put(r, true);
                table.refresh();
                updateDashboardMetrics();

                if (revenueCard != null) {
                    ScaleTransition st1 = new ScaleTransition(Duration.millis(150), revenueCard);
                    st1.setToX(1.04);
                    st1.setToY(1.04);
                    ScaleTransition st2 = new ScaleTransition(Duration.millis(150), revenueCard);
                    st2.setToX(1.0);
                    st2.setToY(1.0);
                    new SequentialTransition(st1, st2).play();
                }

                saveData(); // Save to MySQL
            }
        });

        btnExt.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r != null) {
                TextInputDialog d = new TextInputDialog("1");
                d.setTitle("Extend Stay");
                d.setHeaderText("Additional Days:");
                d.setContentText("Enter number of days:");
                d.showAndWait().ifPresent(days -> {
                    r.extendStay(Integer.parseInt(days));
                    table.refresh();
                    saveData(); // Save to MySQL
                });
            }
        });

        btnOut.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r == null)
                return;

            long days = ChronoUnit.DAYS.between(r.getCheckInDate(), LocalDate.now());
            if (days == 0)
                days = 1;

            double subtotal = days * r.getPrice();
            double tax = subtotal * (gstRate / 100.0);
            double resTotal = 0.0;
            List<RestaurantOrder> settledNow = new ArrayList<>();
            for (RestaurantOrder o : restaurantOrderList) {
                if (o.getRoomNumber().equals(r.getRoomNumber()) && !o.isSettled()) {
                    resTotal += o.getTotalPrice();
                    settledNow.add(o);
                }
            }
            double grandTotal = subtotal + tax + resTotal;

            HistoryRecord record = new HistoryRecord(
                    r.getRoomNumber(), r.getRoomType(), r.getCustomerName(), r.getContactNumber(),
                    r.getGuestEmail(), r.getGuestAddress(),
                    r.getCheckInDate().toString(), LocalDate.now().toString(),
                    r.getPrice(), days, subtotal, tax, gstRate, grandTotal,
                    LocalDateTime.now(), r.getAadhaarImage());

            // ── Step 1: Checkout Preview Dialog ──────────────────────────────────
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Checkout Confirmation");
            dialog.setHeaderText("Checkout preview for Room " + r.getRoomNumber());

            GridPane previewGrid = new GridPane();
            previewGrid.setHgap(10);
            previewGrid.setVgap(10);
            previewGrid.setPadding(new Insets(20, 150, 10, 10));
            previewGrid.add(new Label("Guest:"), 0, 0);
            previewGrid.add(new Label(record.getGuestName()), 1, 0);
            previewGrid.add(new Label("Email:"), 0, 1);
            previewGrid.add(new Label(
                    record.getGuestEmail() != null && !record.getGuestEmail().isEmpty() ? record.getGuestEmail() : "—"),
                    1, 1);
            previewGrid.add(new Label("Check-In:"), 0, 2);
            previewGrid.add(new Label(record.getCheckInDate()), 1, 2);
            previewGrid.add(new Label("Nights:"), 0, 3);
            previewGrid.add(new Label(String.valueOf(record.getNights())), 1, 3);
            previewGrid.add(new Label("Room Subtotal:"), 0, 4);
            previewGrid.add(new Label(String.format("₹ %.2f", record.getSubtotal())), 1, 4);
            previewGrid.add(new Label("GST (" + (int) gstRate + "%):"), 0, 5);
            previewGrid.add(new Label(String.format("₹ %.2f", record.getTaxAmount())), 1, 5);
            previewGrid.add(new Label("Dining/POS:"), 0, 6);
            previewGrid.add(new Label(String.format("₹ %.2f", resTotal)), 1, 6);
            Label totalLabel = new Label(String.format("₹ %.2f", record.getTotalPaid()));
            totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            previewGrid.add(new Label("Grand Total:"), 0, 7);
            previewGrid.add(totalLabel, 1, 7);

            dialog.getDialogPane().setContent(previewGrid);
            ButtonType btnConfirmPdf = new ButtonType("Confirm & Generate Invoice", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnConfirmPdf, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != btnConfirmPdf)
                return;

            // ── Step 2: Choose where to save the PDF ─────────────────────────────
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Invoice PDF");
            fileChooser.setInitialFileName(String.format("Invoice_Room%s_%s.pdf",
                    r.getRoomNumber(), r.getCustomerName().replaceAll("[^a-zA-Z0-9]", "")));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(btnOut.getScene().getWindow());
            if (file == null) {
                showAlert(Alert.AlertType.INFORMATION, "Cancelled", "Checkout cancelled \u2014 no file selected.");
                return;
            }

            // ── Step 3: Generate PDF & commit checkout ────────────────────────────
            try {
                for (RestaurantOrder o : settledNow)
                    o.settledProperty().set(true);
                restaurantOrderList.removeAll(settledNow);

                generateInvoicePDF(file, record, settledNow, resTotal);

                historyList.add(record);
                r.setCheckingOut();
                table.refresh();
                updateDashboardMetrics();
                saveData();
                saveRestaurantOrders();

            } catch (Throwable ex) {
                // Rollback settled orders on failure
                for (RestaurantOrder o : settledNow) {
                    o.settledProperty().set(false);
                    restaurantOrderList.add(o);
                }
                showAlert(Alert.AlertType.ERROR, "Checkout Failed",
                        "Could not complete checkout or generate PDF:\n" + ex.getMessage());
                ex.printStackTrace();
                return;
            }

            // ── Step 4: Post-checkout Action Dialog ──────────────────────────────
            boolean hasEmail = record.getGuestEmail() != null && !record.getGuestEmail().isEmpty();

            ButtonType btnSendEmail = new ButtonType("📧 Send Invoice via Email", ButtonBar.ButtonData.LEFT);
            ButtonType btnSaveAnother = new ButtonType("🖨️ Save Another Copy", ButtonBar.ButtonData.OTHER);
            ButtonType btnDone = new ButtonType("✔ Done", ButtonBar.ButtonData.OK_DONE);

            Alert postDialog = new Alert(Alert.AlertType.INFORMATION);
            postDialog.setTitle("Checkout Complete");
            postDialog.setHeaderText("✅ Invoice saved successfully");
            postDialog.setContentText("PDF saved to:\n" + file.getAbsolutePath() +
                    (hasEmail ? "\n\nRegistered email: " + record.getGuestEmail()
                            : "\n\n⚠ No email on file for this guest."));

            if (hasEmail) {
                postDialog.getButtonTypes().setAll(btnSendEmail, btnSaveAnother, btnDone);
            } else {
                postDialog.getButtonTypes().setAll(btnSaveAnother, btnDone);
            }

            Optional<ButtonType> postResult = postDialog.showAndWait();

            if (postResult.isPresent() && postResult.get() == btnSendEmail) {
                // ── Send email on background thread ──────────────────────────────
                String guestEmail = record.getGuestEmail();
                File invoiceFile = file;
                new Thread(() -> {
                    try {
                        EmailSender.sendInvoice(guestEmail, record.getGuestName(), record.getRoomNumber(), invoiceFile);
                        javafx.application.Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Email Sent",
                                "Invoice successfully emailed to:\n" + guestEmail));
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Email Failed",
                                "Invoice was saved but the email could not be sent.\n\nReason: " + ex.getMessage()));
                    }
                }).start();

            } else if (postResult.isPresent() && postResult.get() == btnSaveAnother) {
                // ── Save a second copy ────────────────────────────────────────────
                FileChooser fc2 = new FileChooser();
                fc2.setTitle("Save Another Copy");
                fc2.setInitialFileName("Copy_" + file.getName());
                fc2.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                File file2 = fc2.showSaveDialog(null);
                if (file2 != null) {
                    try {
                        generateInvoicePDF(file2, record, settledNow, resTotal);
                        showAlert(Alert.AlertType.INFORMATION, "Saved", "Copy saved to:\n" + file2.getAbsolutePath());
                    } catch (Exception ex2) {
                        showAlert(Alert.AlertType.ERROR, "Save Failed", ex2.getMessage());
                    }
                }
            }
        });

        VBox right = new VBox(20, actionCard);
        right.setPadding(new Insets(0, 10, 10, 10));
        VBox.setVgrow(right, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.NEVER);
        right.setPrefWidth(340);

        VBox left = new VBox(12, filterBar, table);
        left.setPadding(new Insets(0, 10, 10, 0));
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox.setHgrow(left, Priority.ALWAYS);

        HBox mainContent = new HBox(15, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.NEVER);

        VBox root = new VBox(20,
                createSectionHeader("Room Bookings", "Manage guest check-in, check-out, and stay extensions"),
                mainContent);
        root.setPadding(new Insets(30));
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        return root;
    }

    @SuppressWarnings("unchecked")
    private Node createInventoryView() {
        TableView<Room> table = new TableView<>();
        styleTable(table);
        table.setItems(roomList);

        TableColumn<Room, String> cNum = new TableColumn<>("Room #");
        cNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        cNum.setStyle("-fx-font-weight: bold;");

        TableColumn<Room, String> cType = new TableColumn<>("Type");
        cType.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<Room, Double> cPrice = new TableColumn<>("Rate (₹/Day)");
        cPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Room, String> cStatus = new TableColumn<>("Status");
        cStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(cNum, cType, cPrice, cStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TextField txtNo = new TextField();
        txtNo.setPromptText("e.g. 101");
        styleTextField(txtNo);
        txtNo.setPrefWidth(120);

        ComboBox<String> comboType = new ComboBox<>(FXCollections.observableArrayList("Single", "Double", "Deluxe"));
        comboType.setPromptText("Select Type");
        comboType.setStyle("-fx-font-size: 13px;");

        Button btnAdd = createStyledButton("+ Add Room", ROYAL_BLUE);
        btnAdd.setTooltip(new Tooltip("Add a new room to inventory"));
        Button btnRemove = createStyledButton("- Remove", DANGER_RED);
        btnRemove.setTooltip(new Tooltip("Remove the selected available room"));

        Button btnImport = createStyledButton("⬆ Import CSV", ROYAL_BLUE);
        btnImport.setTooltip(new Tooltip("Import rooms from a CSV file"));
        btnImport.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            java.io.File file = fc.showOpenDialog(null);
            if (file != null) {
                int added = 0;
                int skipped = 0;
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                    String line = br.readLine(); // skip header
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty())
                            continue;
                        String[] parts = line.split(",", -1);
                        if (parts.length < 1) {
                            skipped++;
                            continue;
                        }
                        String rn = parts[0].trim();
                        if (rn.isEmpty() || roomList.stream().anyMatch(r -> r.getRoomNumber().equals(rn))) {
                            skipped++;
                            continue;
                        }
                        String rt = parts.length > 1 ? parts[1].trim() : "";
                        if (!"Single".equals(rt) && !"Double".equals(rt) && !"Deluxe".equals(rt)) {
                            System.err.println("Warning: Invalid room type '" + rt + "'");
                            skipped++;
                            continue;
                        }
                        double p = 0;
                        if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                            try {
                                p = Double.parseDouble(parts[2].trim());
                            } catch (NumberFormatException ex) {
                            }
                        }
                        if (p <= 0) {
                            p = rt.equals("Single") ? priceSingle : (rt.equals("Double") ? priceDouble : priceDeluxe);
                        }
                        String st = parts.length > 3 ? parts[3].trim() : "";
                        if (!"Available".equals(st) && !"Occupied".equals(st)) {
                            st = "Available";
                        }

                        Room newRoom = new Room(rn, rt, p);
                        newRoom.setStatus(st);
                        roomList.add(newRoom);
                        added++;
                    }
                    updateDashboardMetrics();
                    saveData();
                    showAlert(Alert.AlertType.INFORMATION, "Import Complete",
                            "Import Complete — " + added + " rooms added, " + skipped + " rows skipped.");
                } catch (java.io.IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Import Error",
                            "Failed to read file. Please ensure it is a valid UTF-8 CSV.");
                }
            }
        });

        btnRemove.setOnAction(e -> {
            Room selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getStatus().equals("Available")) {
                roomList.remove(selected);
                updateDashboardMetrics();
                saveData(); // Save to MySQL
            } else {
                showAlert(Alert.AlertType.WARNING, "Cannot Remove",
                        "Select an available (unoccupied) room to remove.");
            }
        });

        btnAdd.setOnAction(e -> {
            if (!txtNo.getText().isEmpty() && comboType.getValue() != null) {
                String newNo = txtNo.getText();
                if (roomList.stream().anyMatch(r -> r.getRoomNumber().equals(newNo))) {
                    showAlert(Alert.AlertType.WARNING, "Duplicate Room", "Room " + newNo + " already exists.");
                    return;
                }
                double p = comboType.getValue().equals("Single") ? priceSingle
                        : (comboType.getValue().equals("Double") ? priceDouble : priceDeluxe);
                roomList.add(new Room(newNo, comboType.getValue(), p));
                txtNo.clear();
                updateDashboardMetrics();
                saveData(); // Save to MySQL
            }
        });

        Label lRoom = new Label("Room No:");
        lRoom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        Label lType = new Label("Type:");
        lType.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");

        HBox form = new HBox(12, lRoom, txtNo, lType, comboType, btnAdd, btnRemove, btnImport);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(15, 20, 15, 20));
        form.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0.0, 0, 2);");

        VBox root = new VBox(20,
                createSectionHeader("Inventory Management", "Add, remove, and review all hotel rooms"),
                form, table);
        root.setPadding(new Insets(30));
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    @SuppressWarnings("unchecked")
    private Node createRestaurantView() {
        TableView<MenuItem> menuTable = new TableView<>();
        styleTable(menuTable);
        menuTable.setItems(menuItemList);

        TableColumn<MenuItem, String> cName = new TableColumn<>("Item");
        cName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        TableColumn<MenuItem, Double> cPrice = new TableColumn<>("Price (₹)");
        cPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        menuTable.getColumns().addAll(cName, cPrice);
        menuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableView<RestaurantOrder> orderTable = new TableView<>();
        styleTable(orderTable);

        orderTable.setItems(restaurantOrderList.filtered(o -> !o.isSettled()));
        restaurantOrderList
                .addListener((javafx.collections.ListChangeListener<RestaurantOrder>) c -> orderTable.refresh());

        TableColumn<RestaurantOrder, String> oRoom = new TableColumn<>("Room");
        oRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<RestaurantOrder, String> oItem = new TableColumn<>("Item");
        oItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        TableColumn<RestaurantOrder, String> oCat = new TableColumn<>("Category");
        oCat.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<RestaurantOrder, Integer> oQty = new TableColumn<>("Qty");
        oQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<RestaurantOrder, Double> oTotal = new TableColumn<>("Total (₹)");
        oTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        TableColumn<RestaurantOrder, LocalDateTime> oTime = new TableColumn<>("Time");
        oTime.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        oTime.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DateTimeFormatter.ofPattern("HH:mm, dd MMM")));
            }
        });

        orderTable.getColumns().addAll(oRoom, oItem, oCat, oQty, oTotal, oTime);
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ComboBox<Room> roomCombo = new ComboBox<>();
        roomCombo.setPromptText("Select Room");

        occupiedRoomsForRestaurant = new javafx.collections.transformation.FilteredList<>(roomList,
                r -> "Occupied".equals(r.getStatus()));
        roomCombo.setItems(occupiedRoomsForRestaurant);

        roomCombo.setConverter(new javafx.util.StringConverter<Room>() {
            @Override
            public String toString(Room r) {
                return r == null ? "" : "Room " + r.getRoomNumber() + " \u2014 " + r.getCustomerName();
            }

            @Override
            public Room fromString(String s) {
                return null;
            }
        });

        roomList.addListener((javafx.collections.ListChangeListener<Room>) change -> {
            occupiedRoomsForRestaurant.setPredicate(r -> "Occupied".equals(r.getStatus()));
            roomCombo.setItems(null);
            roomCombo.setItems(occupiedRoomsForRestaurant);
        });

        Spinner<Integer> qtySpinner = new Spinner<>(1, 20, 1);
        qtySpinner.setPrefWidth(80);

        Button btnOrder = createStyledButton("Place Order", "#2980b9");
        btnOrder.setOnAction(e -> {
            MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
            Room selectedRoom = roomCombo.getValue();

            if (selectedItem != null && selectedRoom != null) {
                String roomNo = selectedRoom.getRoomNumber();
                String guestName = selectedRoom.getCustomerName();
                int qty = qtySpinner.getValue();
                double total = qty * selectedItem.getUnitPrice();

                RestaurantOrder order = new RestaurantOrder(0, roomNo, guestName, selectedItem.getItemCode(),
                        selectedItem.getItemName(), selectedItem.getCategory(), selectedItem.getUnitPrice(),
                        qty, total, LocalDateTime.now(), false);

                restaurantOrderList.add(order);
                saveRestaurantOrders();
                orderTable.refresh();
            } else {
                showAlert(Alert.AlertType.WARNING, "Selection Missing",
                        "Please select an occupied room and a menu item.");
            }
        });

        HBox actionBox = new HBox(15, new Label("Room:"), roomCombo, new Label("Qty:"), qtySpinner, btnOrder);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        actionBox.setPadding(new Insets(15));
        actionBox.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        Button btnImportMenu = createStyledButton("⬆ Import Menu", "#2980b9");
        btnImportMenu.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            java.io.File file = fc.showOpenDialog(null);
            if (file != null) {
                int added = 0;
                int skipped = 0;
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                    String line = br.readLine();
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty())
                            continue;
                        String[] parts = line.split(",", -1);
                        if (parts.length < 1 || parts[0].trim().isEmpty()) {
                            skipped++;
                            continue;
                        }
                        String itemName = parts[0].trim();
                        if (menuItemList.stream().anyMatch(m -> m.getItemName().equals(itemName))) {
                            skipped++;
                            continue;
                        }
                        String category = parts.length > 1 ? parts[1].trim() : "";
                        if (!"Starter".equals(category) && !"Main Course".equals(category)
                                && !"Dessert".equals(category) && !"Beverage".equals(category)) {
                            category = "Main Course";
                        }
                        double price = 0;
                        try {
                            if (parts.length > 2)
                                price = Double.parseDouble(parts[2].trim());
                        } catch (NumberFormatException ex) {
                            price = -1;
                        }
                        if (price <= 0) {
                            skipped++;
                            continue;
                        }

                        boolean available = true;
                        if (parts.length > 3) {
                            String av = parts[3].trim().toLowerCase();
                            if (av.equals("false"))
                                available = false;
                        }
                        if (!available) {
                            skipped++;
                            continue;
                        }

                        String newCode = "M" + (menuItemList.size() + 101);
                        menuItemList.add(new MenuItem(newCode, itemName, category, price));
                        added++;
                    }
                    saveMenuItems();
                    showAlert(Alert.AlertType.INFORMATION, "Import Complete",
                            "Menu Import Complete — " + added + " items added, " + skipped + " rows skipped.");
                } catch (java.io.IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Import Error",
                            "Failed to read menu file. Please ensure it is a valid UTF-8 CSV.");
                }
            }
        });

        HBox menuHeader = new HBox(15, new Label("Menu"), btnImportMenu);
        menuHeader.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(15, menuHeader, menuTable, actionBox);
        HBox.setHgrow(left, Priority.ALWAYS);
        VBox right = new VBox(15, new Label("Pending Room Orders"), orderTable);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox split = new HBox(20, left, right);
        VBox root = new VBox(20, createSectionHeader("Dining & POS", "Manage restaurant orders for checked-in guests"),
                split);
        root.setPadding(new Insets(30));
        return root;
    }

    // --- Housekeeping View ---
    private Node createHousekeepingView() {
        TableView<Room> table = new TableView<>();
        styleTable(table);

        FilteredList<Room> cleaningData = new FilteredList<>(roomList,
                r -> "Cleaning".equals(r.getStatus()) || "Urgent Cleaning".equals(r.getStatus()));
        table.setItems(cleaningData);

        TableColumn<Room, String> cRoom = new TableColumn<>("Room #");
        cRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        cRoom.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TableColumn<Room, String> cType = new TableColumn<>("Type");
        cType.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<Room, String> cStatus = new TableColumn<>("Status");
        cStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        cStatus.setCellFactory(tc -> new TableCell<Room, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label("Urgent Cleaning".equals(item) ? "🔴 URGENT" : "Cleaning");
                    if ("Urgent Cleaning".equals(item)) {
                        badge.setStyle(
                                "-fx-background-color: #fdecea; -fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
                    } else {
                        badge.setStyle(
                                "-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<Room, Void> cAction = new TableColumn<>("Action");
        cAction.setCellFactory(tc -> new TableCell<Room, Void>() {
            private final Button btnClean = new Button("Mark as Available");
            {
                btnClean.setStyle(
                        "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 6;");
                btnClean.setOnAction(e -> {
                    Room r = getTableView().getItems().get(getIndex());
                    r.setStatus("Available");
                    r.setPriority(false);
                    updateDashboardMetrics();
                    saveData();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                    setGraphic(btnClean);
            }
        });

        table.getColumns().addAll(cRoom, cType, cStatus, cAction);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        table.setRowFactory(tv -> new TableRow<Room>() {
            @Override
            protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if ("Urgent Cleaning".equals(item.getStatus())) {
                    setStyle("-fx-background-color: #fff0f0;");
                } else {
                    setStyle("");
                }
            }
        });

        VBox root = new VBox(20, createSectionHeader("Housekeeping", "Manage room cleaning states post-checkout"),
                table);
        root.setPadding(new Insets(30));
        VBox.setVgrow(table, Priority.ALWAYS);

        refreshHousekeepingPane = () -> {
            cleaningData.setPredicate(null);
            cleaningData.setPredicate(r -> "Cleaning".equals(r.getStatus()) || "Urgent Cleaning".equals(r.getStatus()));
            table.refresh();
        };

        return root;
    }

    @SuppressWarnings("unchecked")
    private Node createHistoryView() {
        TableView<HistoryRecord> table = new TableView<>();
        styleTable(table);
        table.setItems(historyList);

        TableColumn<HistoryRecord, String> c1 = new TableColumn<>("Room #");
        c1.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        c1.setStyle("-fx-font-weight: bold;");

        TableColumn<HistoryRecord, String> c2 = new TableColumn<>("Type");
        c2.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<HistoryRecord, String> c3 = new TableColumn<>("Guest Name");
        c3.setCellValueFactory(new PropertyValueFactory<>("guestName"));

        TableColumn<HistoryRecord, String> c_contact = new TableColumn<>("Contact");
        c_contact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        TableColumn<HistoryRecord, String> c4 = new TableColumn<>("Check-In");
        c4.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));

        TableColumn<HistoryRecord, String> c5 = new TableColumn<>("Check-Out");
        c5.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));

        TableColumn<HistoryRecord, Long> c6 = new TableColumn<>("Nights");
        c6.setCellValueFactory(new PropertyValueFactory<>("nights"));

        TableColumn<HistoryRecord, Double> c7 = new TableColumn<>("Amount Paid (Rs)");
        c7.setCellValueFactory(new PropertyValueFactory<>("totalPaid"));
        c7.setCellFactory(tc -> new TableCell<HistoryRecord, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null)
                    setText(null);
                else
                    setText(String.format("Rs. %.2f", value));
            }
        });

        TableColumn<HistoryRecord, LocalDateTime> c8 = new TableColumn<>("Booked At");
        c8.setCellValueFactory(new PropertyValueFactory<>("bookedAt"));
        c8.setCellFactory(tc -> new TableCell<HistoryRecord, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null)
                    setText(null);
                else
                    setText(value.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
            }
        });

        TableColumn<HistoryRecord, byte[]> c9 = new TableColumn<>("ID");
        c9.setCellValueFactory(new PropertyValueFactory<>("aadhaarImage"));
        c9.setCellFactory(tc -> new TableCell<HistoryRecord, byte[]>() {
            @Override
            protected void updateItem(byte[] fileBytes, boolean empty) {
                super.updateItem(fileBytes, empty);
                if (empty)
                    setGraphic(null);
                else if (fileBytes == null)
                    setGraphic(new Label("—"));
                else {
                    ImageView iv = new ImageView(new Image(new ByteArrayInputStream(fileBytes)));
                    iv.setFitWidth(50);
                    iv.setFitHeight(35);
                    iv.setPreserveRatio(true);
                    setGraphic(iv);
                }
            }
        });

        TableColumn<HistoryRecord, Void> c10 = new TableColumn<>("Invoice");
        c10.setCellFactory(tc -> new TableCell<HistoryRecord, Void>() {
            private final Button btn = new Button("⬇ PDF");
            {
                btn.setStyle(
                        "-fx-background-color: #2a1a3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    HistoryRecord data = getTableView().getItems().get(getIndex());
                    FileChooser fc = new FileChooser();
                    fc.setInitialFileName("Reinvoice_Room" + data.getRoomNumber() + "_" + data.getGuestName() + ".pdf");
                    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"));
                    File f = fc.showSaveDialog(null);
                    if (f == null)
                        return;

                    try {
                        generateInvoicePDF(f, data, null, 0.0);
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "PDF Error", ex.getMessage());
                        return;
                    }

                    boolean hasEmail = data.getGuestEmail() != null && !data.getGuestEmail().isEmpty();
                    ButtonType btnMail = new ButtonType("📧 Send via Email", ButtonBar.ButtonData.LEFT);
                    ButtonType btnCopy = new ButtonType("🖨️ Save Another Copy", ButtonBar.ButtonData.OTHER);
                    ButtonType btnClose = new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);

                    Alert post = new Alert(Alert.AlertType.INFORMATION);
                    post.setTitle("Invoice Saved");
                    post.setHeaderText("Re-invoice saved for " + data.getGuestName());
                    post.setContentText("File: " + f.getAbsolutePath() +
                            (hasEmail ? "\nEmail on file: " + data.getGuestEmail() : "\n⚠ No email on file."));
                    post.getButtonTypes().setAll(hasEmail ? btnMail : btnCopy, btnCopy, btnClose);
                    if (!hasEmail)
                        post.getButtonTypes().remove(btnMail);

                    Optional<ButtonType> res = post.showAndWait();
                    if (res.isPresent() && res.get() == btnMail && hasEmail) {
                        String email = data.getGuestEmail();
                        new Thread(() -> {
                            try {
                                EmailSender.sendInvoice(email, data.getGuestName(), data.getRoomNumber(), f);
                                javafx.application.Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION,
                                        "Email Sent", "Invoice emailed to: " + email));
                            } catch (Exception ex) {
                                javafx.application.Platform.runLater(
                                        () -> showAlert(Alert.AlertType.WARNING, "Email Failed", ex.getMessage()));
                            }
                        }).start();
                    } else if (res.isPresent() && res.get() == btnCopy) {
                        FileChooser fc2 = new FileChooser();
                        fc2.setInitialFileName("Copy_" + f.getName());
                        fc2.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                        File f2 = fc2.showSaveDialog(null);
                        if (f2 != null) {
                            try {
                                generateInvoicePDF(f2, data, null, 0.0);
                            } catch (Exception ex2) {
                                showAlert(Alert.AlertType.ERROR, "Error", ex2.getMessage());
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                    setGraphic(btn);
            }
        });

        table.getColumns().addAll(c1, c2, c3, c_contact, c4, c5, c6, c7, c8, c9, c10);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Revenue summary bar
        ledgerSummaryLabel = new Label();
        ledgerSummaryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        ledgerSummaryLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(ledgerSummaryLabel, Priority.ALWAYS);

        // Auto-update when history changes
        historyList.addListener((javafx.collections.ListChangeListener<HistoryRecord>) change -> {
            updateLedgerSummary();
        });

        Button btnExport = createStyledButton("⬇ Export Full Ledger as CSV", ROYAL_BLUE);
        btnExport.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Ledger CSV");
            fileChooser.setInitialFileName("MIT_Grand_Regency_Ledger_" + LocalDate.now().toString() + ".csv");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println(
                            "Room#,Type,Guest Name,Contact,Check-In,Check-Out,Nights,Rate,Subtotal,GST%,Tax,Grand Total,Booked At");
                    for (HistoryRecord hr : historyList) {
                        writer.println(hr.toCSV());
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                            "Ledger exported to:\n" + file.getAbsolutePath());
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Export Failed", ex.getMessage());
                }
            }
        });

        HBox summaryBar = new HBox(15, ledgerSummaryLabel, btnExport);
        summaryBar.setAlignment(Pos.CENTER_LEFT);
        summaryBar.setPadding(new Insets(14, 20, 14, 20));
        summaryBar.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0.0, 0, 2);");

        VBox root = new VBox(20,
                createSectionHeader("Financial Ledger", "Complete checkout history and revenue records"), summaryBar,
                table);
        root.setPadding(new Insets(30));
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    private Node createSettingsView() {
        TextField tSin = new TextField(String.valueOf(priceSingle));
        styleTextField(tSin);
        TextField tDou = new TextField(String.valueOf(priceDouble));
        styleTextField(tDou);
        TextField tDel = new TextField(String.valueOf(priceDeluxe));
        styleTextField(tDel);
        TextField tGst = new TextField(String.valueOf(gstRate));
        styleTextField(tGst);

        Button btnSave = createStyledButton("Save Configuration", GOLD_ACCENT);
        btnSave.setStyle(btnSave.getStyle() + "-fx-text-fill: " + DARK_TEXT + "; -fx-font-weight: bold;");

        btnSave.setOnAction(e -> {
            try {
                priceSingle = Double.parseDouble(tSin.getText());
                priceDouble = Double.parseDouble(tDou.getText());
                priceDeluxe = Double.parseDouble(tDel.getText());
                gstRate = Double.parseDouble(tGst.getText());
                saveSettings();
                showAlert(Alert.AlertType.INFORMATION, "Settings Saved",
                        "Global pricing and GST updated successfully.\nNote: Existing rooms retain their original locked prices.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric values.");
            }
        });

        GridPane grid = new GridPane();
        grid.setVgap(18);
        grid.setHgap(20);
        grid.setPadding(new Insets(25));

        String labelStyle = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";";
        Label l1 = new Label("Single Room Rate (₹):");
        l1.setStyle(labelStyle);
        Label l2 = new Label("Double Room Rate (₹):");
        l2.setStyle(labelStyle);
        Label l3 = new Label("Deluxe Room Rate (₹):");
        l3.setStyle(labelStyle);
        Label l4 = new Label("Global GST Rate (%):");
        l4.setStyle(labelStyle);

        grid.add(l1, 0, 0);
        grid.add(tSin, 1, 0);
        grid.add(l2, 0, 1);
        grid.add(tDou, 1, 1);
        grid.add(l3, 0, 2);
        grid.add(tDel, 1, 2);
        grid.add(l4, 0, 3);
        grid.add(tGst, 1, 3);
        grid.add(btnSave, 1, 5);

        // Wrap settings in styled card
        VBox settingsCard = new VBox(15, grid);
        settingsCard.setPadding(new Insets(10));
        settingsCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.0, 0, 4);");
        settingsCard.setMaxWidth(500);

        VBox root = new VBox(25,
                createSectionHeader("System Configuration", "Manage room pricing and tax rates"),
                settingsCard);
        root.setPadding(new Insets(35));
        return root;
    }

    // --- Utility: Style Helpers ---
    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 18;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: derive(" + color + ", -15%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 18;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.0, 0, 2);"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 18;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"));
        return btn;
    }

    private void styleTextField(TextField tf) {
        // Inline styles moved to styles.css targeting .text-field
    }

    private <T> void styleTable(TableView<T> table) {
        // Inline styles moved to styles.css targeting .table-view
        // We ensure .table-view class is added and fixed cell size
        table.getStyleClass().add("table-view");
        table.setFixedCellSize(40);
    }

    // --- Persistence ---
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private void loadSettings() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT setting_key, setting_value FROM settings")) {
            while (rs.next()) {
                String key = rs.getString("setting_key");
                String value = rs.getString("setting_value");
                switch (key) {
                    case "priceSingle":
                        priceSingle = Double.parseDouble(value);
                        break;
                    case "priceDouble":
                        priceDouble = Double.parseDouble(value);
                        break;
                    case "priceDeluxe":
                        priceDeluxe = Double.parseDouble(value);
                        break;
                    case "gstRate":
                        gstRate = Double.parseDouble(value);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveSettings() {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String[][] settings = {
                    { "priceSingle", String.valueOf(priceSingle) },
                    { "priceDouble", String.valueOf(priceDouble) },
                    { "priceDeluxe", String.valueOf(priceDeluxe) },
                    { "gstRate", String.valueOf(gstRate) }
            };

            for (String[] setting : settings) {
                pstmt.setString(1, setting[0]);
                pstmt.setString(2, setting[1]);
                pstmt.setString(3, setting[1]);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safeGetString(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (Exception e) {
            return null;
        }
    }

    private double safeGetDouble(ResultSet rs, String col) {
        try {
            return rs.getDouble(col);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private long safeGetLong(ResultSet rs, String col) {
        try {
            return rs.getLong(col);
        } catch (Exception e) {
            return 0L;
        }
    }

    private byte[] safeGetBytes(ResultSet rs, String col) {
        try {
            return rs.getBytes(col);
        } catch (Exception e) {
            return null;
        }
    }

    private Timestamp safeGetTimestamp(ResultSet rs, String col) {
        try {
            return rs.getTimestamp(col);
        } catch (Exception e) {
            return null;
        }
    }

    private void loadData() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM rooms");
            while (rs.next()) {
                LocalDate inDate = rs.getDate("check_in_date") != null ? rs.getDate("check_in_date").toLocalDate()
                        : null;
                LocalDate outDate = rs.getDate("expected_checkout_date") != null
                        ? rs.getDate("expected_checkout_date").toLocalDate()
                        : null;

                String checkOutTimeString = null;
                try {
                    checkOutTimeString = rs.getString("checkout_time");
                } catch (Exception ignored) {
                } // Ignore if column doesn't exist yet

                Room r = new Room(
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getDouble("price"),
                        rs.getString("status"),
                        rs.getString("customer_name"),
                        rs.getString("contact_number"),
                        rs.getString("guest_email"),
                        rs.getString("guest_address"),
                        inDate,
                        outDate,
                        rs.getBytes("aadhaar_image"),
                        checkOutTimeString);

                try {
                    r.setPriority(rs.getBoolean("priority"));
                } catch (Exception ignored) {
                } // Ignore if column doesn't exist yet

                roomList.add(r);
            }

            ResultSet rsHist = stmt.executeQuery("SELECT * FROM checkout_history");
            while (rsHist.next()) {
                Timestamp ts = safeGetTimestamp(rsHist, "booked_at");
                LocalDateTime booked = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                historyList.add(new HistoryRecord(
                        rsHist.getString("room_number"),
                        safeGetString(rsHist, "room_type"),
                        rsHist.getString("guest_name"),
                        safeGetString(rsHist, "contact_number"),
                        safeGetString(rsHist, "guest_email"),
                        safeGetString(rsHist, "guest_address"),
                        safeGetString(rsHist, "check_in_date"),
                        safeGetString(rsHist, "checkout_date"),
                        safeGetDouble(rsHist, "price_per_night"),
                        safeGetLong(rsHist, "nights"),
                        safeGetDouble(rsHist, "subtotal"),
                        safeGetDouble(rsHist, "tax_amount"),
                        safeGetDouble(rsHist, "gst_rate"),
                        safeGetDouble(rsHist, "total_paid"), // Assume total_paid always exists since it's an old col
                        booked,
                        safeGetBytes(rsHist, "aadhaar_image")));
            }
            ResultSet rsMenu = stmt.executeQuery("SELECT * FROM menu_items");
            while (rsMenu.next()) {
                menuItemList.add(new MenuItem(
                        rsMenu.getString("item_code"),
                        rsMenu.getString("item_name"),
                        rsMenu.getString("category"),
                        rsMenu.getDouble("unit_price")));
            }

            ResultSet rsOrders = stmt.executeQuery("SELECT * FROM restaurant_orders");
            while (rsOrders.next()) {
                String roomNum = rsOrders.getString("room_number");
                boolean isOccupied = roomList.stream()
                        .anyMatch(r -> r.getRoomNumber().equals(roomNum) && "Occupied".equals(r.getStatus()));
                if (!isOccupied)
                    continue;

                Timestamp tsOrder = rsOrders.getTimestamp("order_time");
                LocalDateTime tOrder = tsOrder != null ? tsOrder.toLocalDateTime() : LocalDateTime.now();
                restaurantOrderList.add(new RestaurantOrder(
                        rsOrders.getInt("id"),
                        rsOrders.getString("room_number"),
                        rsOrders.getString("guest_name"),
                        rsOrders.getString("item_code"),
                        rsOrders.getString("item_name"),
                        rsOrders.getString("category"),
                        rsOrders.getDouble("unit_price"),
                        rsOrders.getInt("quantity"),
                        rsOrders.getDouble("total_price"),
                        tOrder,
                        rsOrders.getBoolean("settled")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        boolean removedOrders = restaurantOrderList.removeIf(o -> roomList.stream()
                .anyMatch(r -> r.getRoomNumber().equals(o.getRoomNumber()) && "Available".equals(r.getStatus())));
        if (removedOrders) {
            saveRestaurantOrders();
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // No longer deleting rooms. We rely on REPLACE INTO with primary key
            // room_number.

            String insertRoom = "REPLACE INTO rooms (room_number, room_type, price, status, customer_name, contact_number, guest_email, guest_address, check_in_date, expected_checkout_date, aadhaar_image, checkout_time, priority) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertRoom)) {
                for (Room r : roomList) {
                    pstmt.setString(1, r.getRoomNumber());
                    pstmt.setString(2, r.getRoomType());
                    pstmt.setDouble(3, r.getPrice());
                    pstmt.setString(4, r.getStatus());
                    pstmt.setString(5, r.getCustomerName());
                    pstmt.setString(6, r.getContactNumber());
                    pstmt.setString(7, r.getGuestEmail());
                    pstmt.setString(8, r.getGuestAddress());
                    pstmt.setDate(9, r.getCheckInDate() != null ? java.sql.Date.valueOf(r.getCheckInDate()) : null);
                    pstmt.setDate(10,
                            r.getExpectedCheckOutDate() != null ? java.sql.Date.valueOf(r.getExpectedCheckOutDate())
                                    : null);
                    pstmt.setBytes(11, r.getAadhaarImage());
                    pstmt.setString(12, r.getCheckOutTime());
                    pstmt.setBoolean(13, r.isPriority());
                    try {
                        pstmt.executeUpdate();
                    } catch (Exception saveEx) {
                        System.err.println("Could not save room " + r.getRoomNumber()
                                + ": maybe checkout_time/priority column missing.");
                    }
                }
            }

            try (Statement clearHist = conn.createStatement()) {
                clearHist.executeUpdate("DELETE FROM checkout_history");
            }

            String insertHist = "REPLACE INTO checkout_history (id, room_number, room_type, guest_name, contact_number, guest_email, guest_address, check_in_date, checkout_date, price_per_night, nights, subtotal, tax_amount, gst_rate, total_paid, booked_at, aadhaar_image) VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertHist)) {
                for (HistoryRecord h : historyList) {
                    pstmt.setString(1, h.getRoomNumber());
                    pstmt.setString(2, h.getRoomType());
                    pstmt.setString(3, h.getGuestName());
                    pstmt.setString(4, h.getContactNumber());
                    pstmt.setString(5, h.getGuestEmail());
                    pstmt.setString(6, h.getGuestAddress());
                    pstmt.setString(7, h.getCheckInDate());
                    pstmt.setString(8, h.getCheckOutDate());
                    pstmt.setDouble(9, h.getPricePerNight());
                    pstmt.setLong(10, h.getNights());
                    pstmt.setDouble(11, h.getSubtotal());
                    pstmt.setDouble(12, h.getTaxAmount());
                    pstmt.setDouble(13, h.getGstRate());
                    pstmt.setDouble(14, h.getTotalPaid());
                    pstmt.setTimestamp(15, h.getBookedAt() != null ? Timestamp.valueOf(h.getBookedAt())
                            : Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.setBytes(16, h.getAadhaarImage());
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveMenuItems() {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM menu_items");
            }
            String insertMenu = "REPLACE INTO menu_items (item_code, item_name, category, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertMenu)) {
                for (MenuItem item : menuItemList) {
                    pstmt.setString(1, item.getItemCode());
                    pstmt.setString(2, item.getItemName());
                    pstmt.setString(3, item.getCategory());
                    pstmt.setDouble(4, item.getUnitPrice());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            conn.commit();
        } catch (Exception e) {
            System.err.println("MySQL Error matching menu items: " + e.getMessage());
        }
    }

    private void saveRestaurantOrders() {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement clearOrders = conn.createStatement()) {
                clearOrders.executeUpdate("DELETE FROM restaurant_orders");
            }
            String insertOrders = "REPLACE INTO restaurant_orders (id, room_number, guest_name, item_code, item_name, category, unit_price, quantity, total_price, order_time, settled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrders)) {
                for (RestaurantOrder o : restaurantOrderList) {
                    if (o.getId() == 0)
                        pstmt.setNull(1, java.sql.Types.INTEGER);
                    else
                        pstmt.setInt(1, o.getId());
                    pstmt.setString(2, o.getRoomNumber());
                    pstmt.setString(3, o.getGuestName());
                    pstmt.setString(4, o.getItemCode());
                    pstmt.setString(5, o.getItemName());
                    pstmt.setString(6, o.getCategory());
                    pstmt.setDouble(7, o.getUnitPrice());
                    pstmt.setInt(8, o.getQuantity());
                    pstmt.setDouble(9, o.getTotalPrice());
                    pstmt.setTimestamp(10, o.getOrderTime() != null ? Timestamp.valueOf(o.getOrderTime())
                            : Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.setBoolean(11, o.isSettled());
                    pstmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateInvoicePDF(File file, HistoryRecord hr, List<RestaurantOrder> orders, double resTotal)
            throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            String invoiceNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // PDFBox 3.x requires RGB values as floats in the range 0.0–1.0 (not 0–255)
            // Helper constants
            float[] GOLD = { 201 / 255f, 169 / 255f, 110 / 255f };
            float[] WHITE = { 1f, 1f, 1f };
            float[] BLACK = { 0f, 0f, 0f };
            float[] GRAY = { 100 / 255f, 100 / 255f, 100 / 255f };

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                // ── Header gold band ────────────────────────────────────────
                cs.setNonStrokingColor(GOLD[0], GOLD[1], GOLD[2]);
                cs.addRect(0, 740, 612, 52);
                cs.fill();

                // Hotel name on header
                cs.beginText();
                cs.setFont(fontBold, 24);
                cs.setNonStrokingColor(WHITE[0], WHITE[1], WHITE[2]);
                cs.newLineAtOffset(50, 755);
                cs.showText("MIT Grand Regency");
                cs.endText();

                // ── Gold rule under header ───────────────────────────────────
                cs.setStrokingColor(GOLD[0], GOLD[1], GOLD[2]);
                cs.setLineWidth(2f);
                cs.moveTo(50, 720);
                cs.lineTo(562, 720);
                cs.stroke();

                // ── Invoice metadata (left column) ──────────────────────────
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontBold, 16);
                cs.newLineAtOffset(50, 690);
                cs.showText("INVOICE");

                cs.setFont(fontNormal, 12);
                cs.newLineAtOffset(0, -20);
                cs.showText("Invoice Number: " + invoiceNumber);
                cs.newLineAtOffset(0, -15);
                cs.showText("Date: " + LocalDate.now().toString());
                cs.newLineAtOffset(0, -15);
                cs.showText("Room: " + hr.getRoomNumber() + " (" + hr.getRoomType() + ")");
                cs.endText();

                // ── Billed To (right column) ─────────────────────────────────
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(350, 690);
                cs.showText("Billed To:");

                cs.setFont(fontNormal, 12);
                cs.newLineAtOffset(0, -15);
                cs.showText(hr.getGuestName());
                cs.newLineAtOffset(0, -15);
                cs.showText("Contact: " + hr.getContactNumber());
                cs.newLineAtOffset(0, -15);
                cs.showText("Check-In:  " + hr.getCheckInDate());
                cs.newLineAtOffset(0, -15);
                cs.showText("Total Nights: " + hr.getNights());
                cs.endText();

                // ── Table header lines ───────────────────────────────────────
                int tableTop = 560;
                cs.setStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setLineWidth(1f);
                cs.moveTo(50, tableTop);
                cs.lineTo(562, tableTop);
                cs.moveTo(50, tableTop - 25);
                cs.lineTo(562, tableTop - 25);
                cs.stroke();

                // Column headers
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(60, tableTop - 16);
                cs.showText("Description");
                cs.newLineAtOffset(240, 0);
                cs.showText("Quantity");
                cs.newLineAtOffset(80, 0);
                cs.showText("Unit Rate");
                cs.newLineAtOffset(100, 0);
                cs.showText("Amount");
                cs.endText();

                // ── Room charge row ──────────────────────────────────────────
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontNormal, 12);
                cs.newLineAtOffset(60, tableTop - 45);
                cs.showText(hr.getRoomType() + " Room (" + hr.getNights() + " nights)");
                cs.newLineAtOffset(240, 0);
                cs.showText("1");
                cs.newLineAtOffset(80, 0);
                cs.showText(String.format("Rs. %.2f", hr.getSubtotal()));
                cs.newLineAtOffset(100, 0);
                cs.showText(String.format("Rs. %.2f", hr.getSubtotal()));
                cs.endText();

                int currentY = tableTop - 65;

                // ── Restaurant line items ──────────────────────────
                if (orders != null && !orders.isEmpty()) {
                    for (RestaurantOrder o : orders) {
                        cs.beginText();
                        cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                        cs.setFont(fontNormal, 12);
                        cs.newLineAtOffset(60, currentY);
                        cs.showText("Dining: " + o.getItemName());
                        cs.newLineAtOffset(240, 0);
                        cs.showText(String.valueOf(o.getQuantity()));
                        cs.newLineAtOffset(80, 0);
                        cs.showText(String.format("Rs. %.2f", o.getUnitPrice()));
                        cs.newLineAtOffset(100, 0);
                        cs.showText(String.format("Rs. %.2f", o.getTotalPrice()));
                        cs.endText();
                        currentY -= 20;
                    }
                }

                // ── GST row ──
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontNormal, 12);
                cs.newLineAtOffset(60, currentY);
                cs.showText(String.format("GST (%.1f%%)", hr.getGstRate()));
                cs.newLineAtOffset(240, 0);
                cs.showText("-");
                cs.newLineAtOffset(80, 0);
                cs.showText("-");
                cs.newLineAtOffset(100, 0);
                cs.showText(String.format("Rs. %.2f", hr.getTaxAmount()));
                cs.endText();

                // ── Table bottom border ──────────────────────────────────────
                cs.setStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.moveTo(50, currentY - 20);
                cs.lineTo(562, currentY - 20);
                cs.stroke();

                // ── Grand total ──────────────────────────────────────────────
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontBold, 14);
                cs.newLineAtOffset(350, currentY - 45);
                cs.showText("Grand Total:");
                cs.newLineAtOffset(100, 0);
                cs.showText(String.format("Rs. %.2f", hr.getTotalPaid()));
                cs.endText();

                // ── Footer ───────────────────────────────────────────────────
                cs.beginText();
                cs.setNonStrokingColor(GRAY[0], GRAY[1], GRAY[2]);
                cs.setFont(fontNormal, 10);
                cs.newLineAtOffset(50, 100);
                cs.showText("Thank you for staying at MIT Grand Regency. We hope to see you again soon.");
                cs.newLineAtOffset(0, -15);
                cs.showText("This is a computer-generated invoice and does not require a physical signature.");
                cs.endText();
            }

            document.save(file);
        }
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.getDialogPane().setStyle(
                "-fx-font-family: monospace;" +
                        "-fx-font-size: 13px;");
        a.getDialogPane().setMinWidth(450);
        a.showAndWait();
    }

    public static class EmailSender {
        // Prerequisite: tharunadithyan@gmail.com must have 2-Step Verification and an
        // App Password.
        private static final String FROM_EMAIL = "tharunadithyan@gmail.com";
        private static final String APP_PASSWORD = "vkgi egxl gfke gyhw";

        public static void sendInvoice(String toEmail, String guestName, String roomNumber, File pdfFile)
                throws Exception {
            System.out.println("[EmailSender] Attempting to connect to SMTP servers for: " + toEmail);

            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "587");
            prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            prop.put("mail.smtp.connectiontimeout", "15000");
            prop.put("mail.smtp.timeout", "15000");

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            try {
                System.out.println("[EmailSender] Authenticating and assembling MIME message...");
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("Your Invoice \u2014 MIT Grand Regency, Room " + roomNumber);

                String htmlBody = "<div style=\"font-family:Georgia,serif;color:#1a1a2e;max-width:600px;\">\n" +
                        "  <div style=\"background:#c9a96e;padding:20px;\">\n" +
                        "    <h1 style=\"color:white;margin:0;\">MIT Grand Regency</h1>\n" +
                        "    <p style=\"color:white;margin:0;font-size:12px;\">Luxury Hospitality Management</p>\n" +
                        "  </div>\n" +
                        "  <div style=\"padding:20px;\">\n" +
                        "    <p>Dear <strong>" + guestName + "</strong>,</p>\n" +
                        "    <p>Thank you for staying with us. Please find your invoice attached for Room <strong>"
                        + roomNumber + "</strong>.</p>\n" +
                        "    <p>We look forward to welcoming you again.</p>\n" +
                        "    <p style=\"color:#c9a96e;font-weight:bold;\">\u2014 MIT Grand Regency Team</p>\n" +
                        "  </div>\n" +
                        "</div>";

                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody, "text/html; charset=utf-8");

                Multipart mp = new MimeMultipart();
                mp.addBodyPart(htmlPart);

                if (pdfFile != null && pdfFile.exists()) {
                    System.out.println("[EmailSender] Attaching PDF Invoice. Size: " + pdfFile.length() + " bytes");
                    MimeBodyPart attachPart = new MimeBodyPart();
                    attachPart.attachFile(pdfFile);
                    attachPart.setFileName("Invoice_Room" + roomNumber + ".pdf");
                    mp.addBodyPart(attachPart);
                }

                message.setContent(mp);

                System.out.println("[EmailSender] Transport.send() triggered...");
                Transport.send(message);
                System.out.println("[EmailSender] Email sent successfully to: " + toEmail);

            } catch (Exception ex) {
                System.err.println("[EmailSender] FAILED: " + ex.getMessage());
                ex.printStackTrace();
                throw new RuntimeException("SMTP send failed: " + ex.getMessage(), ex);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

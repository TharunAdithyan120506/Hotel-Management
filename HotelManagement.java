import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Properties;

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

        public Room(String no, String type, double price, String status, String name, String contact, LocalDate checkIn,
                LocalDate checkOut) {
            this.roomNumber.set(no);
            this.roomType.set(type);
            this.price.set(price);
            this.status.set(status);
            this.customerName.set(name);
            this.contactNumber.set(contact);
            this.checkInDate.set(checkIn);
            this.expectedCheckOutDate.set(checkOut);
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

        public void setStatus(String s) {
            this.status.set(s);
        }

        public void setOccupancy(String n, String c, LocalDate in, LocalDate out) {
            this.customerName.set(n);
            this.contactNumber.set(c);
            this.checkInDate.set(in);
            this.expectedCheckOutDate.set(out);
        }

        public void extendStay(int days) {
            if (expectedCheckOutDate.get() != null) {
                expectedCheckOutDate.set(expectedCheckOutDate.get().plusDays(days));
            }
        }

        public void clearOccupancy() {
            this.customerName.set("");
            this.contactNumber.set("");
            this.checkInDate.set(null);
            this.expectedCheckOutDate.set(null);
            this.status.set("Available");
        }

        public String toCSV() {
            return String.join(",", getRoomNumber(), getRoomType(), String.valueOf(getPrice()), getStatus(),
                    getCustomerName(), getContactNumber(),
                    checkInDate.get() != null ? checkInDate.get().toString() : "null",
                    expectedCheckOutDate.get() != null ? expectedCheckOutDate.get().toString() : "null");
        }
    }

    public static class HistoryRecord {

        private final StringProperty roomNumber = new SimpleStringProperty();
        private final StringProperty guestName = new SimpleStringProperty();
        private final StringProperty checkOut = new SimpleStringProperty();
        private final DoubleProperty totalPaid = new SimpleDoubleProperty();

        public HistoryRecord(String room, String guest, String out, double paid) {
            this.roomNumber.set(room);
            this.guestName.set(guest);
            this.checkOut.set(out);
            this.totalPaid.set(paid);
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

        public String getCheckOut() {
            return checkOut.get();
        }

        public StringProperty checkOutProperty() {
            return checkOut;
        }

        public double getTotalPaid() {
            return totalPaid.get();
        }

        public DoubleProperty totalPaidProperty() {
            return totalPaid;
        }

        public String toCSV() {
            return String.join(",", getRoomNumber(), getGuestName(), getCheckOut(), String.valueOf(getTotalPaid()));
        }
    }

    // --- State ---
    private final ObservableList<Room> roomList = FXCollections.observableArrayList();
    private final ObservableList<HistoryRecord> historyList = FXCollections.observableArrayList();
    private final StackPane contentArea = new StackPane();

    // UI Metrics
    private final Label lblTotal = new Label("0"), lblAvail = new Label("0"), lblOcc = new Label("0"),
            lblRevenue = new Label("₹0");

    // Ledger summary label (needs class-level reference for dynamic updates)
    private Label ledgerSummaryLabel;

    // Dashboard activity center (needs class-level reference for dynamic updates)
    private FlowPane activityCenter;

    // Track active nav button
    private Button activeNavButton = null;

    @Override
    public void start(Stage primaryStage) {
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

        // Nav buttons
        Button btnDash = createNavButton("\u2302  Dashboard");
        Button btnBook = createNavButton("\u270E  Bookings");
        Button btnInv = createNavButton("\u2630  Inventory");
        Button btnLedger = createNavButton("\u2261  Ledger");
        Button btnSet = createNavButton("\u2699  Settings");

        VBox navItems = new VBox(4);
        navItems.setPadding(new Insets(0, 10, 20, 10));
        navItems.getChildren().addAll(btnDash, btnBook, btnInv, btnLedger, btnSet);

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
        Node viewLedger = createHistoryView();
        Node viewSet = createSettingsView();

        contentArea.setStyle("-fx-background-color: " + BG_WARM + ";");
        contentArea.getChildren().addAll(viewDash, viewBook, viewInv, viewLedger, viewSet);
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

        // Global CSS for tables and controls
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;");

        primaryStage.setTitle("MIT Grand Regency — Luxury Hospitality Management");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> saveData());
        primaryStage.show();

        updateDashboardMetrics();
    }

    // --- Styled Navigation Button ---
    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefHeight(42);
        applyNavStyle(btn, false);

        btn.setOnMouseEntered(e -> {
            if (btn != activeNavButton) {
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
        btn.setOnMouseExited(e -> {
            if (btn != activeNavButton) {
                applyNavStyle(btn, false);
            }
        });
        return btn;
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
        }
        activeNavButton = btn;
        applyNavStyle(btn, true);
    }

    private void switchView(Node view) {
        for (Node n : contentArea.getChildren()) {
            n.setVisible(false);
        }
        view.setVisible(true);
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

    // --- Bento-box tile for occupied room ---
    private VBox createOccupiedRoomTile(Room room) {
        Label roomNo = new Label("Room " + room.getRoomNumber());
        roomNo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");

        Label roomType = new Label(room.getRoomType());
        roomType.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GOLD_ACCENT + "; -fx-font-weight: bold;");

        Label guestLabel = new Label("\u263A " + room.getCustomerName());
        guestLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        Label contactLabel = new Label("\u260E " + room.getContactNumber());
        contactLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        String checkoutStr = room.getExpectedCheckOutDate() != null ? room.getExpectedCheckOutDate().toString() : "N/A";
        Label checkoutLabel = new Label("Checkout: " + checkoutStr);
        checkoutLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        // Days remaining
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

        // Gold top accent bar
        Region topAccent = new Region();
        topAccent.setPrefHeight(3);
        topAccent.setMaxWidth(Double.MAX_VALUE);
        topAccent.setStyle("-fx-background-color: " + GOLD_ACCENT + "; -fx-background-radius: 3 3 0 0;");

        VBox tile = new VBox(5, topAccent, roomNo, roomType, guestLabel, contactLabel, checkoutLabel, daysLabel);
        tile.setPadding(new Insets(0, 14, 12, 14));
        tile.setPrefWidth(195);
        tile.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0.0, 0, 3);");
        return tile;
    }

    // --- Refresh Activity Center ---
    private void refreshActivityCenter() {
        if (activityCenter == null)
            return;
        activityCenter.getChildren().clear();

        long occupiedCount = roomList.stream().filter(r -> r.getStatus().equals("Occupied")).count();
        if (occupiedCount == 0) {
            Label empty = new Label("No rooms currently occupied.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa; -fx-padding: 30;");
            activityCenter.getChildren().add(empty);
        } else {
            for (Room r : roomList) {
                if (r.getStatus().equals("Occupied")) {
                    activityCenter.getChildren().add(createOccupiedRoomTile(r));
                }
            }
        }
    }

    // --- View Generators ---
    private Node createDashboardView() {
        // Stats row — brand-consistent dark navy palette
        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER_LEFT);
        cards.getChildren().addAll(
                createCard("Total Rooms", lblTotal, "#1a1a2e", "#16213e", "\u2302"),
                createCard("Available", lblAvail, "#1a3c34", "#1e5245", "\u2714"),
                createCard("Occupied", lblOcc, "#3c1a1a", "#522020", "\u263A"),
                createCard("Revenue", lblRevenue, "#2a1a3c", "#3d2452", "\u2605"));

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

        // Bento-box FlowPane
        activityCenter = new FlowPane();
        activityCenter.setHgap(14);
        activityCenter.setVgap(14);
        activityCenter.setPadding(new Insets(5));
        refreshActivityCenter();

        ScrollPane scrollPane = new ScrollPane(activityCenter);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox activityCard = new VBox(12, actHeaderBox, scrollPane);
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
        return root;
    }

    private void updateDashboardMetrics() {
        lblTotal.setText(String.valueOf(roomList.size()));
        lblAvail.setText(String.valueOf(roomList.stream().filter(r -> r.getStatus().equals("Available")).count()));
        lblOcc.setText(String.valueOf(roomList.stream().filter(r -> r.getStatus().equals("Occupied")).count()));
        double totalRevenue = historyList.stream().mapToDouble(HistoryRecord::getTotalPaid).sum();
        lblRevenue.setText(String.format("\u20B9%.0f", totalRevenue));

        // Refresh bento-box activity center
        refreshActivityCenter();

        // Update ledger summary bar
        updateLedgerSummary();
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
        TableView<Room> table = new TableView<>();
        styleTable(table);
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

        table.getColumns().addAll(colRoom, colType, colStat, colGuest, colContact, colIn, colOut);
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

        Button btnBook = createStyledButton("Book", "#1a5e3a");
        Button btnExt = createStyledButton("Extend", "#7a5c1e");
        Button btnOut = createStyledButton("Checkout", "#7a1e1e");
        btnExt.setDisable(true);
        btnOut.setDisable(true);

        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(12);
        grid.setPadding(new Insets(15));

        Label lName = new Label("Guest Name");
        lName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        Label lPhone = new Label("Phone");
        lPhone.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        Label lDays = new Label("Duration (Days)");
        lDays.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");

        grid.add(lName, 0, 0);
        grid.add(txtCust, 0, 1);
        grid.add(lPhone, 0, 2);
        grid.add(txtCont, 0, 3);
        grid.add(lDays, 0, 4);
        grid.add(spinDays, 0, 5);

        HBox btnRow = new HBox(10, btnBook, btnExt, btnOut);
        btnRow.setPadding(new Insets(8, 0, 0, 0));
        grid.add(btnRow, 0, 6);

        // Wrap action panel in styled card
        Label actionTitle = new Label("Action Desk");
        actionTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        actionTitle.setStyle("-fx-text-fill: " + DARK_TEXT + ";");

        Region actionSep = new Region();
        actionSep.setPrefHeight(2);
        actionSep.setMaxWidth(Double.MAX_VALUE);
        actionSep.setStyle("-fx-background-color: " + GOLD_ACCENT + "; -fx-opacity: 0.5;");

        VBox actionCard = new VBox(10, actionTitle, actionSep, grid);
        actionCard.setPadding(new Insets(20));
        actionCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.0, 0, 3);");
        actionCard.setPrefWidth(320);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                if (sel.getStatus().equals("Occupied")) {
                    txtCust.setText(sel.getCustomerName());
                    txtCont.setText(sel.getContactNumber());
                    txtCust.setEditable(false);
                    txtCont.setEditable(false);
                    spinDays.setDisable(true);
                    btnBook.setDisable(true);
                    btnExt.setDisable(false);
                    btnOut.setDisable(false);
                } else {
                    txtCust.clear();
                    txtCont.clear();
                    txtCust.setEditable(true);
                    txtCont.setEditable(true);
                    spinDays.setDisable(false);
                    btnBook.setDisable(false);
                    btnExt.setDisable(true);
                    btnOut.setDisable(true);
                }
            }
        });

        btnBook.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r != null && !txtCust.getText().isEmpty()) {
                r.setStatus("Occupied");
                r.setOccupancy(txtCust.getText(), txtCont.getText(), LocalDate.now(),
                        LocalDate.now().plusDays(spinDays.getValue()));
                table.refresh();
                updateDashboardMetrics();
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
            if (r != null) {
                long days = ChronoUnit.DAYS.between(r.getCheckInDate(), LocalDate.now());
                if (days == 0) {
                    days = 1;
                }
                double subtotal = days * r.getPrice();
                double tax = subtotal * (gstRate / 100.0);
                double grandTotal = subtotal + tax;

                historyList.add(new HistoryRecord(r.getRoomNumber(), r.getCustomerName(), LocalDate.now().toString(),
                        grandTotal));

                String invoice = String.format(
                        "╔══════════════════════════════════╗\n" +
                                "║     MIT GRAND REGENCY            ║\n" +
                                "║        FINAL INVOICE             ║\n" +
                                "╠══════════════════════════════════╣\n" +
                                "║ Guest: %-25s ║\n" +
                                "║ Room:  %-25s ║\n" +
                                "║ Days:  %-25d ║\n" +
                                "║ Rate:  ₹%-24.2f ║\n" +
                                "╠══════════════════════════════════╣\n" +
                                "║ Subtotal:  ₹%-20.2f ║\n" +
                                "║ GST %.1f%%:  ₹%-20.2f ║\n" +
                                "╠══════════════════════════════════╣\n" +
                                "║ GRAND TOTAL: ₹%-18.2f ║\n" +
                                "╚══════════════════════════════════╝",
                        r.getCustomerName(), r.getRoomNumber(), days, r.getPrice(),
                        subtotal, gstRate, tax, grandTotal);

                r.clearOccupancy();
                table.refresh();
                updateDashboardMetrics();
                saveData(); // Save to MySQL
                showAlert(Alert.AlertType.INFORMATION, "Checkout Complete", invoice);
            }
        });

        VBox right = new VBox(20, actionCard);
        right.setPadding(new Insets(0, 10, 10, 10));

        VBox left = new VBox(12, filterBar, table);
        left.setPadding(new Insets(0, 10, 10, 0));
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox.setHgrow(left, Priority.ALWAYS);

        HBox mainContent = new HBox(15, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);

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
        Button btnRemove = createStyledButton("- Remove", DANGER_RED);

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
                roomList.add(new Room(newNo, comboType.getValue(), p, "Available", "", "", null, null));
                txtNo.clear();
                updateDashboardMetrics();
                saveData(); // Save to MySQL
            }
        });

        Label lRoom = new Label("Room No:");
        lRoom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        Label lType = new Label("Type:");
        lType.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");

        HBox form = new HBox(12, lRoom, txtNo, lType, comboType, btnAdd, btnRemove);
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
    private Node createHistoryView() {
        TableView<HistoryRecord> table = new TableView<>();
        styleTable(table);
        table.setItems(historyList);

        TableColumn<HistoryRecord, String> c1 = new TableColumn<>("Room #");
        c1.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        c1.setStyle("-fx-font-weight: bold;");

        TableColumn<HistoryRecord, String> c2 = new TableColumn<>("Guest Name");
        c2.setCellValueFactory(new PropertyValueFactory<>("guestName"));

        TableColumn<HistoryRecord, String> c3 = new TableColumn<>("Checkout Date");
        c3.setCellValueFactory(new PropertyValueFactory<>("checkOut"));

        TableColumn<HistoryRecord, Double> c4 = new TableColumn<>("Amount Paid (₹)");
        c4.setCellValueFactory(new PropertyValueFactory<>("totalPaid"));

        table.getColumns().addAll(c1, c2, c3, c4);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Revenue summary bar (dynamically updated)
        ledgerSummaryLabel = new Label();
        ledgerSummaryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        double totalRev = historyList.stream().mapToDouble(HistoryRecord::getTotalPaid).sum();
        ledgerSummaryLabel.setText(
                String.format("Total Revenue: \u20B9%.2f  |  Total Checkouts: %d", totalRev, historyList.size()));

        // Auto-update when history changes
        historyList.addListener((javafx.collections.ListChangeListener<HistoryRecord>) change -> {
            updateLedgerSummary();
        });
        Label revLabel = ledgerSummaryLabel;

        HBox summaryBar = new HBox(revLabel);
        summaryBar.setPadding(new Insets(14, 20, 14, 20));
        summaryBar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0.0, 0, 2);");

        VBox root = new VBox(20,
                createSectionHeader("Financial Ledger", "Complete checkout history and revenue records"),
                summaryBar, table);
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
        tf.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-padding: 8 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #ddd;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-color: #fefefe;");
        tf.setOnMouseEntered(e -> tf.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-padding: 8 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: " + GOLD_ACCENT + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-color: white;"));
        tf.setOnMouseExited(e -> {
            if (!tf.isFocused()) {
                tf.setStyle(
                        "-fx-font-size: 13px;" +
                                "-fx-padding: 8 12;" +
                                "-fx-background-radius: 8;" +
                                "-fx-border-color: #ddd;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-color: #fefefe;");
            }
        });
    }

    private <T> void styleTable(TableView<T> table) {
        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e8e8e8;" +
                        "-fx-border-radius: 12;" +
                        "-fx-font-size: 13px;");
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

                roomList.add(new Room(
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getDouble("price"),
                        rs.getString("status"),
                        rs.getString("customer_name"),
                        rs.getString("contact_number"),
                        inDate,
                        outDate));
            }

            ResultSet rsHist = stmt
                    .executeQuery("SELECT room_number, guest_name, checkout_date, total_paid FROM checkout_history");
            while (rsHist.next()) {
                historyList.add(new HistoryRecord(
                        rsHist.getString("room_number"),
                        rsHist.getString("guest_name"),
                        rsHist.getString("checkout_date"),
                        rsHist.getDouble("total_paid")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (Statement clearRooms = conn.createStatement()) {
                clearRooms.executeUpdate("DELETE FROM rooms");
            }

            String insertRoom = "REPLACE INTO rooms (room_number, room_type, price, status, customer_name, contact_number, check_in_date, expected_checkout_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertRoom)) {
                for (Room r : roomList) {
                    pstmt.setString(1, r.getRoomNumber());
                    pstmt.setString(2, r.getRoomType());
                    pstmt.setDouble(3, r.getPrice());
                    pstmt.setString(4, r.getStatus());
                    pstmt.setString(5, r.getCustomerName());
                    pstmt.setString(6, r.getContactNumber());
                    pstmt.setDate(7, r.getCheckInDate() != null ? java.sql.Date.valueOf(r.getCheckInDate()) : null);
                    pstmt.setDate(8,
                            r.getExpectedCheckOutDate() != null ? java.sql.Date.valueOf(r.getExpectedCheckOutDate())
                                    : null);
                    pstmt.executeUpdate();
                }
            }

            try (Statement clearHist = conn.createStatement()) {
                clearHist.executeUpdate("DELETE FROM checkout_history");
            }

            String insertHist = "REPLACE INTO checkout_history (id, room_number, guest_name, checkout_date, total_paid) VALUES (NULL, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertHist)) {
                for (HistoryRecord h : historyList) {
                    pstmt.setString(1, h.getRoomNumber());
                    pstmt.setString(2, h.getGuestName());
                    pstmt.setString(3, h.getCheckOut());
                    pstmt.setDouble(4, h.getTotalPaid());
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
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

    public static void main(String[] args) {
        launch(args);
    }
}
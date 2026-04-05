package com.mitgrandregency.hotel.ui;

import com.mitgrandregency.hotel.dao.*;
import com.mitgrandregency.hotel.model.AppState;
import com.mitgrandregency.hotel.service.*;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    private AppState state;
    private DashboardView dashboardView;
    private LedgerView ledgerView;
    private HousekeepingView housekeepingView;
    private RestaurantView restaurantView;
    private RoomDAO roomDAO;
    private OrderDAO orderDAO;
    private HistoryDAO historyDAO;
    private final StackPane contentArea = new StackPane();
    private final Button[] activeNavButton = { null };

    @Override
    public void start(Stage primaryStage) {
        // --- Wire dependencies ---
        state = new AppState();
        ConfigLoader config = new ConfigLoader();

        // Apply config defaults to state
        state.setPriceSingle(config.getPriceSingle());
        state.setPriceDouble(config.getPriceDouble());
        state.setPriceDeluxe(config.getPriceDeluxe());
        state.setGstRate(config.getGstRate());

        DatabaseManager dbManager = new DatabaseManager(config);
        dbManager.ensureSchema();

        roomDAO = new RoomDAO(dbManager);
        historyDAO = new HistoryDAO(dbManager);
        MenuDAO menuDAO = new MenuDAO(dbManager);
        orderDAO = new OrderDAO(dbManager);
        SettingsDAO settingsDAO = new SettingsDAO(dbManager);

        // Load DB settings (overrides config file defaults)
        settingsDAO.loadSettings(state);

        // Load data
        roomDAO.loadAll(state.getRoomList());
        historyDAO.loadAll(state.getHistoryList());
        menuDAO.loadAll(state.getMenuItemList());
        orderDAO.loadAll(state.getRoomList(), state.getRestaurantOrderList());

        // Services
        AadhaarStorageService aadhaarService = new AadhaarStorageService();
        BookingService bookingService = new BookingService(state);
        InvoiceService invoiceService = new InvoiceService();
        EmailService emailService = new EmailService(config);
        ReportService reportService = new ReportService(state);

        // onDataChanged callback
        Runnable onDataChanged = this::onDataChanged;

        // Views
        dashboardView = new DashboardView(state, reportService, aadhaarService);
        BookingsView bookingsView = new BookingsView(state, bookingService,
                aadhaarService, invoiceService, emailService, roomDAO, orderDAO, onDataChanged);
        InventoryView inventoryView = new InventoryView(state, roomDAO, onDataChanged);
        restaurantView = new RestaurantView(state, menuDAO, orderDAO);
        housekeepingView = new HousekeepingView(state, roomDAO, onDataChanged);
        ledgerView = new LedgerView(state, invoiceService, emailService, reportService);
        SettingsView settingsView = new SettingsView(state, settingsDAO);

        // --- Sidebar ---
        VBox sidebar = new VBox(0);
        sidebar.setStyle("-fx-background-color:linear-gradient(to bottom,"
                + UIUtils.SIDEBAR_TOP + "," + UIUtils.SIDEBAR_BOTTOM + ");");
        sidebar.setPrefWidth(240);

        VBox logoBox = new VBox(2);
        logoBox.setPadding(new Insets(30, 20, 10, 20));
        logoBox.setAlignment(Pos.CENTER_LEFT);
        Label logo = new Label("MIT Grand Regency");
        logo.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        logo.setStyle("-fx-text-fill:" + UIUtils.GOLD_ACCENT + ";");
        Label tagline = new Label("Luxury Hospitality Management");
        tagline.setFont(Font.font("Georgia", FontWeight.NORMAL, 11));
        tagline.setStyle("-fx-text-fill:" + UIUtils.IVORY_TEXT + ";-fx-opacity:0.7;");
        logoBox.getChildren().addAll(logo, tagline);

        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setMaxWidth(Double.MAX_VALUE);
        separator.setStyle("-fx-background-color:" + UIUtils.GOLD_ACCENT + ";-fx-opacity:0.4;");
        VBox.setMargin(separator, new Insets(15, 20, 15, 20));

        StackPane paneDash  = UIUtils.createNavButton("\u2302  Dashboard", activeNavButton);
        StackPane paneBook  = UIUtils.createNavButton("\u270E  Bookings", activeNavButton);
        StackPane paneInv   = UIUtils.createNavButton("\u2630  Inventory", activeNavButton);
        StackPane paneRest  = UIUtils.createNavButton("\uD83C\uDF7D  Dining & POS", activeNavButton);
        StackPane paneHouse = UIUtils.createNavButton("\u2728  Housekeeping", activeNavButton);
        StackPane paneLedger= UIUtils.createNavButton("\u2261  Ledger", activeNavButton);
        StackPane paneSet   = UIUtils.createNavButton("\u2699  Settings", activeNavButton);

        Button btnDash  = (Button) paneDash.getUserData();
        Button btnBook  = (Button) paneBook.getUserData();
        Button btnInv   = (Button) paneInv.getUserData();
        Button btnRest  = (Button) paneRest.getUserData();
        Button btnHouse = (Button) paneHouse.getUserData();
        Button btnLedger= (Button) paneLedger.getUserData();
        Button btnSet   = (Button) paneSet.getUserData();

        VBox navItems = new VBox(4);
        navItems.setPadding(new Insets(0, 10, 20, 10));
        navItems.getChildren().addAll(paneDash, paneBook, paneInv, paneRest, paneHouse, paneLedger, paneSet);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label footer = new Label("Tharun Adithyan OSDL Project");
        footer.setStyle("-fx-text-fill:" + UIUtils.IVORY_TEXT + ";-fx-font-size:10px;-fx-opacity:0.4;");
        footer.setPadding(new Insets(0, 0, 15, 20));
        sidebar.getChildren().addAll(logoBox, separator, navItems, spacer, footer);

        // --- Content Views ---
        Node viewDash  = dashboardView.createView();
        Node viewBook  = bookingsView.createView();
        Node viewInv   = inventoryView.createView();
        Node viewRest  = restaurantView.createView();
        Node viewHouse = housekeepingView.createView();
        Node viewLedger= ledgerView.createView();
        Node viewSet   = settingsView.createView();

        contentArea.setStyle("-fx-background-color:" + UIUtils.BG_WARM + ";");
        contentArea.getChildren().addAll(viewDash, viewBook, viewInv, viewRest, viewHouse, viewLedger, viewSet);
        switchView(viewDash);
        UIUtils.setActiveNav(btnDash, activeNavButton);

        btnDash.setOnAction(e -> { switchView(viewDash); UIUtils.setActiveNav(btnDash, activeNavButton); dashboardView.updateMetrics(); });
        btnBook.setOnAction(e -> { switchView(viewBook); UIUtils.setActiveNav(btnBook, activeNavButton); });
        btnInv.setOnAction(e -> { switchView(viewInv); UIUtils.setActiveNav(btnInv, activeNavButton); });
        btnRest.setOnAction(e -> { switchView(viewRest); UIUtils.setActiveNav(btnRest, activeNavButton); });
        btnHouse.setOnAction(e -> { switchView(viewHouse); UIUtils.setActiveNav(btnHouse, activeNavButton); });
        btnLedger.setOnAction(e -> { switchView(viewLedger); UIUtils.setActiveNav(btnLedger, activeNavButton); });
        btnSet.setOnAction(e -> { switchView(viewSet); UIUtils.setActiveNav(btnSet, activeNavButton); });

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1250, 750);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        scene.getRoot().setStyle("-fx-font-family:'Segoe UI','Helvetica Neue',Arial,sans-serif;");

        primaryStage.setTitle("MIT Grand Regency \u2014 Luxury Hospitality Management");

        WritableImage icon = new WritableImage(64, 64);
        PixelWriter pw = icon.getPixelWriter();
        for (int x = 0; x < 64; x++)
            for (int y = 0; y < 64; y++)
                pw.setColor(x, y, (x > 20 && x < 44 && y > 20 && y < 44)
                        ? Color.web(UIUtils.GOLD_ACCENT) : Color.web("#1a1a2e"));
        primaryStage.getIcons().add(icon);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> saveAllData());

        primaryStage.setOpacity(0);
        primaryStage.show();
        new Timeline(new KeyFrame(Duration.millis(400),
                new KeyValue(primaryStage.opacityProperty(), 1.0))).play();

        dashboardView.updateMetrics();
    }

    private void onDataChanged() {
        dashboardView.updateMetrics();
        ledgerView.updateSummary();
        roomDAO.saveAll(state.getRoomList());
        historyDAO.saveAll(state.getHistoryList());

        var filter = restaurantView.getOccupiedRoomsFilter();
        if (filter != null) {
            filter.setPredicate(null);
            filter.setPredicate(r -> "Occupied".equals(r.getStatus()));
        }
        var hkRefresh = housekeepingView.getRefreshCallback();
        if (hkRefresh != null) hkRefresh.run();
    }

    private void saveAllData() {
        state.getRestaurantOrderList().removeIf(o ->
                state.getRoomList().stream().anyMatch(
                        r -> r.getRoomNumber().equals(o.getRoomNumber())
                                && "Available".equals(r.getStatus())));
        roomDAO.saveAll(state.getRoomList());
        orderDAO.saveAll(state.getRestaurantOrderList());
        historyDAO.saveAll(state.getHistoryList());
    }

    private void switchView(Node view) {
        Node outgoing = contentArea.getChildren().stream()
                .filter(Node::isVisible).findFirst().orElse(null);
        if (outgoing == view) return;
        if (outgoing != null) {
            FadeTransition ftOut = new FadeTransition(Duration.millis(120), outgoing);
            ftOut.setFromValue(1.0); ftOut.setToValue(0.0);
            FadeTransition ftIn = new FadeTransition(Duration.millis(200), view);
            ftIn.setFromValue(0.0); ftIn.setToValue(1.0);
            ftOut.setOnFinished(e -> outgoing.setVisible(false));
            view.setOpacity(0.0); view.setVisible(true);
            new SequentialTransition(ftOut, ftIn).play();
        } else {
            contentArea.getChildren().forEach(n -> n.setVisible(false));
            view.setOpacity(1.0); view.setVisible(true);
        }
    }

    public static void main(String[] args) { launch(args); }
}

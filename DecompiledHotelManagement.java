/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javafx.application.Application
 *  javafx.beans.property.DoubleProperty
 *  javafx.beans.property.ObjectProperty
 *  javafx.beans.property.SimpleDoubleProperty
 *  javafx.beans.property.SimpleObjectProperty
 *  javafx.beans.property.SimpleStringProperty
 *  javafx.beans.property.StringProperty
 *  javafx.collections.FXCollections
 *  javafx.collections.ObservableList
 *  javafx.collections.transformation.FilteredList
 *  javafx.geometry.Insets
 *  javafx.geometry.Pos
 *  javafx.scene.Node
 *  javafx.scene.Parent
 *  javafx.scene.Scene
 *  javafx.scene.control.Alert
 *  javafx.scene.control.Alert$AlertType
 *  javafx.scene.control.Button
 *  javafx.scene.control.ComboBox
 *  javafx.scene.control.Label
 *  javafx.scene.control.ScrollPane
 *  javafx.scene.control.Spinner
 *  javafx.scene.control.TableCell
 *  javafx.scene.control.TableColumn
 *  javafx.scene.control.TableView
 *  javafx.scene.control.TextField
 *  javafx.scene.control.TextInputDialog
 *  javafx.scene.control.cell.PropertyValueFactory
 *  javafx.scene.layout.BorderPane
 *  javafx.scene.layout.FlowPane
 *  javafx.scene.layout.GridPane
 *  javafx.scene.layout.HBox
 *  javafx.scene.layout.Priority
 *  javafx.scene.layout.Region
 *  javafx.scene.layout.StackPane
 *  javafx.scene.layout.VBox
 *  javafx.scene.text.Font
 *  javafx.scene.text.FontWeight
 *  javafx.stage.Stage
 *  javafx.util.Callback
 */
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

public class HotelManagement
extends Application {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/mit_grand_regency";
    private static final String DB_USER = "tharun";
    private static final String DB_PASS = "password123";
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
    private double priceSingle = 1000.0;
    private double priceDouble = 2000.0;
    private double priceDeluxe = 3500.0;
    private double gstRate = 18.0;
    private final ObservableList<Room> roomList = FXCollections.observableArrayList();
    private final ObservableList<HistoryRecord> historyList = FXCollections.observableArrayList();
    private final StackPane contentArea = new StackPane();
    private final Label lblTotal = new Label("0");
    private final Label lblAvail = new Label("0");
    private final Label lblOcc = new Label("0");
    private final Label lblRevenue = new Label("\u20b90");
    private Label ledgerSummaryLabel;
    private FlowPane activityCenter;
    private Button activeNavButton = null;

    public void start(Stage stage) {
        this.loadSettings();
        this.loadData();
        VBox vBox = new VBox(0.0);
        vBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");
        vBox.setPrefWidth(240.0);
        VBox vBox2 = new VBox(2.0);
        vBox2.setPadding(new Insets(30.0, 20.0, 10.0, 20.0));
        vBox2.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("MIT Grand Regency");
        label.setFont(Font.font((String)"Georgia", (FontWeight)FontWeight.BOLD, (double)22.0));
        label.setStyle("-fx-text-fill: #c9a96e;");
        Label label2 = new Label("Luxury Hospitality Management");
        label2.setFont(Font.font((String)"Georgia", (FontWeight)FontWeight.NORMAL, (double)11.0));
        label2.setStyle("-fx-text-fill: #f0e6d3; -fx-opacity: 0.7;");
        vBox2.getChildren().addAll((Object[])new Node[]{label, label2});
        Region region = new Region();
        region.setPrefHeight(1.0);
        region.setMaxWidth(Double.MAX_VALUE);
        region.setStyle("-fx-background-color: #c9a96e; -fx-opacity: 0.4;");
        VBox.setMargin((Node)region, (Insets)new Insets(15.0, 20.0, 15.0, 20.0));
        Button button = this.createNavButton("\u2302  Dashboard");
        Button button2 = this.createNavButton("\u270e  Bookings");
        Button button3 = this.createNavButton("\u2630  Inventory");
        Button button4 = this.createNavButton("\u2261  Ledger");
        Button button5 = this.createNavButton("\u2699  Settings");
        VBox vBox3 = new VBox(4.0);
        vBox3.setPadding(new Insets(0.0, 10.0, 20.0, 10.0));
        vBox3.getChildren().addAll((Object[])new Node[]{button, button2, button3, button4, button5});
        Region region2 = new Region();
        VBox.setVgrow((Node)region2, (Priority)Priority.ALWAYS);
        Label label3 = new Label("Tharun Adithyan OSDL Project");
        label3.setStyle("-fx-text-fill: #f0e6d3; -fx-font-size: 10px; -fx-opacity: 0.4;");
        label3.setPadding(new Insets(0.0, 0.0, 15.0, 20.0));
        vBox.getChildren().addAll((Object[])new Node[]{vBox2, region, vBox3, region2, label3});
        Node node = this.createDashboardView();
        Node node2 = this.createBookingsView();
        Node node3 = this.createInventoryView();
        Node node4 = this.createHistoryView();
        Node node5 = this.createSettingsView();
        this.contentArea.setStyle("-fx-background-color: #faf8f5;");
        this.contentArea.getChildren().addAll((Object[])new Node[]{node, node2, node3, node4, node5});
        this.switchView(node);
        this.setActiveNav(button);
        button.setOnAction(actionEvent -> {
            this.switchView(node);
            this.setActiveNav(button);
            this.updateDashboardMetrics();
        });
        button2.setOnAction(actionEvent -> {
            this.switchView(node2);
            this.setActiveNav(button2);
        });
        button3.setOnAction(actionEvent -> {
            this.switchView(node3);
            this.setActiveNav(button3);
        });
        button4.setOnAction(actionEvent -> {
            this.switchView(node4);
            this.setActiveNav(button4);
        });
        button5.setOnAction(actionEvent -> {
            this.switchView(node5);
            this.setActiveNav(button5);
        });
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft((Node)vBox);
        borderPane.setCenter((Node)this.contentArea);
        Scene scene = new Scene((Parent)borderPane, 1250.0, 750.0);
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;");
        stage.setTitle("MIT Grand Regency \u2014 Luxury Hospitality Management");
        stage.setScene(scene);
        stage.setOnCloseRequest(windowEvent -> this.saveData());
        stage.show();
        this.updateDashboardMetrics();
    }

    private Button createNavButton(String string) {
        Button button = new Button(string);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPrefHeight(42.0);
        this.applyNavStyle(button, false);
        button.setOnMouseEntered(mouseEvent -> {
            if (button != this.activeNavButton) {
                button.setStyle("-fx-background-color: rgba(201,169,110,0.15);-fx-text-fill: #c9a96e;-fx-font-size: 14px;-fx-alignment: center-left;-fx-padding: 10 20;-fx-background-radius: 8;-fx-cursor: hand;");
            }
        });
        button.setOnMouseExited(mouseEvent -> {
            if (button != this.activeNavButton) {
                this.applyNavStyle(button, false);
            }
        });
        return button;
    }

    private void applyNavStyle(Button button, boolean bl) {
        if (bl) {
            button.setStyle("-fx-background-color: rgba(201,169,110,0.25);-fx-text-fill: #c9a96e;-fx-font-size: 14px;-fx-font-weight: bold;-fx-alignment: center-left;-fx-padding: 10 20;-fx-background-radius: 8;-fx-border-color: transparent transparent transparent #c9a96e;-fx-border-width: 0 0 0 3;-fx-border-radius: 8;");
        } else {
            button.setStyle("-fx-background-color: transparent;-fx-text-fill: #f0e6d3;-fx-font-size: 14px;-fx-alignment: center-left;-fx-padding: 10 20;-fx-background-radius: 8;-fx-cursor: hand;");
        }
    }

    private void setActiveNav(Button button) {
        if (this.activeNavButton != null) {
            this.applyNavStyle(this.activeNavButton, false);
        }
        this.activeNavButton = button;
        this.applyNavStyle(button, true);
    }

    private void switchView(Node node) {
        for (Node node2 : this.contentArea.getChildren()) {
            node2.setVisible(false);
        }
        node.setVisible(true);
    }

    private VBox createSectionHeader(String string, String string2) {
        VBox vBox = new VBox(4.0);
        vBox.setPadding(new Insets(0.0, 0.0, 10.0, 0.0));
        Label label = new Label(string);
        label.setFont(Font.font((String)"Georgia", (FontWeight)FontWeight.BOLD, (double)26.0));
        label.setStyle("-fx-text-fill: #2c2c2c;");
        Label label2 = new Label(string2);
        label2.setFont(Font.font((String)"Segoe UI", (FontWeight)FontWeight.NORMAL, (double)13.0));
        label2.setStyle("-fx-text-fill: #888888;");
        Region region = new Region();
        region.setPrefHeight(3.0);
        region.setPrefWidth(60.0);
        region.setMaxWidth(60.0);
        region.setStyle("-fx-background-color: #c9a96e; -fx-background-radius: 2;");
        vBox.getChildren().addAll((Object[])new Node[]{label, label2, region});
        return vBox;
    }

    private VBox createCard(String string, Label label, String string2, String string3, String string4) {
        Label label2 = new Label(string4);
        label2.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 38px;");
        Label label3 = new Label(string);
        label3.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 34px; -fx-font-weight: bold;");
        VBox vBox = new VBox(4.0, new Node[]{label3, label});
        vBox.setAlignment(Pos.CENTER_LEFT);
        HBox hBox = new HBox(12.0, new Node[]{vBox, label2});
        hBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow((Node)vBox, (Priority)Priority.ALWAYS);
        VBox vBox2 = new VBox(new Node[]{hBox});
        vBox2.setPadding(new Insets(20.0, 22.0, 20.0, 22.0));
        vBox2.setPrefHeight(110.0);
        vBox2.setStyle("-fx-background-color: linear-gradient(to bottom right, " + string2 + ", " + string3 + ");-fx-background-radius: 14;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0.0, 0, 4);");
        HBox.setHgrow((Node)vBox2, (Priority)Priority.ALWAYS);
        return vBox2;
    }

    private VBox createOccupiedRoomTile(Room room) {
        Label label = new Label("Room " + room.getRoomNumber());
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;");
        Label label2 = new Label(room.getRoomType());
        label2.setStyle("-fx-font-size: 11px; -fx-text-fill: #c9a96e; -fx-font-weight: bold;");
        Label label3 = new Label("\u263a " + room.getCustomerName());
        label3.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        Label label4 = new Label("\u260e " + room.getContactNumber());
        label4.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        String string = room.getExpectedCheckOutDate() != null ? room.getExpectedCheckOutDate().toString() : "N/A";
        Label label5 = new Label("Checkout: " + string);
        label5.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        Object object = "";
        if (room.getExpectedCheckOutDate() != null) {
            long l = ChronoUnit.DAYS.between(LocalDate.now(), room.getExpectedCheckOutDate());
            if (l <= 0L) {
                object = "OVERDUE";
                label5.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else {
                object = l == 1L ? "1 day left" : l + " days left";
            }
        }
        Label label6 = new Label((String)object);
        label6.setStyle("-fx-font-size: 10px; -fx-text-fill: #c9a96e; -fx-font-weight: bold;");
        Region region = new Region();
        region.setPrefHeight(3.0);
        region.setMaxWidth(Double.MAX_VALUE);
        region.setStyle("-fx-background-color: #c9a96e; -fx-background-radius: 3 3 0 0;");
        VBox vBox = new VBox(5.0, new Node[]{region, label, label2, label3, label4, label5, label6});
        vBox.setPadding(new Insets(0.0, 14.0, 12.0, 14.0));
        vBox.setPrefWidth(195.0);
        vBox.setStyle("-fx-background-color: white;-fx-background-radius: 10;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0.0, 0, 3);");
        return vBox;
    }

    private void refreshActivityCenter() {
        if (this.activityCenter == null) {
            return;
        }
        this.activityCenter.getChildren().clear();
        long l = this.roomList.stream().filter(room -> room.getStatus().equals("Occupied")).count();
        if (l == 0L) {
            Label label = new Label("No rooms currently occupied.");
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa; -fx-padding: 30;");
            this.activityCenter.getChildren().add((Object)label);
        } else {
            for (Room room2 : this.roomList) {
                if (!room2.getStatus().equals("Occupied")) continue;
                this.activityCenter.getChildren().add((Object)this.createOccupiedRoomTile(room2));
            }
        }
    }

    private Node createDashboardView() {
        HBox hBox = new HBox(16.0);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().addAll((Object[])new Node[]{this.createCard("Total Rooms", this.lblTotal, SIDEBAR_TOP, SIDEBAR_BOTTOM, "\u2302"), this.createCard("Available", this.lblAvail, "#1a3c34", "#1e5245", "\u2714"), this.createCard("Occupied", this.lblOcc, "#3c1a1a", "#522020", "\u263a"), this.createCard("Revenue", this.lblRevenue, "#2a1a3c", "#3d2452", "\u2605")});
        Label label = new Label("Activity Center \u2014 Live Occupancy");
        label.setFont(Font.font((String)"Georgia", (FontWeight)FontWeight.BOLD, (double)18.0));
        label.setStyle("-fx-text-fill: #2c2c2c;");
        Label label2 = new Label("Rooms currently in use with guest details");
        label2.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        Region region = new Region();
        region.setPrefHeight(2.0);
        region.setPrefWidth(50.0);
        region.setMaxWidth(50.0);
        region.setStyle("-fx-background-color: #c9a96e; -fx-background-radius: 2;");
        VBox vBox = new VBox(4.0, new Node[]{label, label2, region});
        this.activityCenter = new FlowPane();
        this.activityCenter.setHgap(14.0);
        this.activityCenter.setVgap(14.0);
        this.activityCenter.setPadding(new Insets(5.0));
        this.refreshActivityCenter();
        ScrollPane scrollPane = new ScrollPane((Node)this.activityCenter);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow((Node)scrollPane, (Priority)Priority.ALWAYS);
        VBox vBox2 = new VBox(12.0, new Node[]{vBox, scrollPane});
        vBox2.setPadding(new Insets(20.0));
        vBox2.setStyle("-fx-background-color: #f5f2ee;-fx-background-radius: 14;");
        VBox.setVgrow((Node)vBox2, (Priority)Priority.ALWAYS);
        VBox vBox3 = new VBox(22.0, new Node[]{this.createSectionHeader("Live Overview", "Real-time hotel occupancy and revenue at a glance"), hBox, vBox2});
        vBox3.setPadding(new Insets(30.0));
        VBox.setVgrow((Node)vBox2, (Priority)Priority.ALWAYS);
        return vBox3;
    }

    private void updateDashboardMetrics() {
        this.lblTotal.setText(String.valueOf(this.roomList.size()));
        this.lblAvail.setText(String.valueOf(this.roomList.stream().filter(room -> room.getStatus().equals("Available")).count()));
        this.lblOcc.setText(String.valueOf(this.roomList.stream().filter(room -> room.getStatus().equals("Occupied")).count()));
        double d = this.historyList.stream().mapToDouble(HistoryRecord::getTotalPaid).sum();
        this.lblRevenue.setText(String.format("\u20b9%.0f", d));
        this.refreshActivityCenter();
        this.updateLedgerSummary();
    }

    private void updateLedgerSummary() {
        if (this.ledgerSummaryLabel != null) {
            double d = this.historyList.stream().mapToDouble(HistoryRecord::getTotalPaid).sum();
            this.ledgerSummaryLabel.setText(String.format("Total Revenue: \u20b9%.2f  |  Total Checkouts: %d", d, this.historyList.size()));
        }
    }

    private Node createBookingsView() {
        TableView tableView = new TableView();
        this.styleTable(tableView);
        FilteredList filteredList = new FilteredList(this.roomList, room -> true);
        tableView.setItems((ObservableList)filteredList);
        TableColumn tableColumn2 = new TableColumn("Room #");
        tableColumn2.setCellValueFactory((Callback)new PropertyValueFactory("roomNumber"));
        tableColumn2.setStyle("-fx-font-weight: bold;");
        TableColumn tableColumn3 = new TableColumn("Type");
        tableColumn3.setCellValueFactory((Callback)new PropertyValueFactory("roomType"));
        TableColumn tableColumn4 = new TableColumn("Status");
        tableColumn4.setCellValueFactory((Callback)new PropertyValueFactory("status"));
        tableColumn4.setCellFactory(tableColumn -> new TableCell<Room, String>(this){
            {
                Objects.requireNonNull(hotelManagement);
            }

            protected void updateItem(String string, boolean bl) {
                super.updateItem((Object)string, bl);
                if (bl || string == null) {
                    this.setText(null);
                    this.setGraphic(null);
                } else {
                    Label label = new Label(string);
                    label.setPadding(new Insets(3.0, 12.0, 3.0, 12.0));
                    if (string.equals("Available")) {
                        label.setStyle("-fx-background-color: #e8f8f0; -fx-text-fill: #27ae60;-fx-font-weight: bold; -fx-font-size: 11px;-fx-background-radius: 12;");
                    } else {
                        label.setStyle("-fx-background-color: #fdecea; -fx-text-fill: #c0392b;-fx-font-weight: bold; -fx-font-size: 11px;-fx-background-radius: 12;");
                    }
                    this.setGraphic((Node)label);
                    this.setText(null);
                }
            }
        });
        TableColumn tableColumn5 = new TableColumn("Guest Name");
        tableColumn5.setCellValueFactory((Callback)new PropertyValueFactory("customerName"));
        TableColumn tableColumn6 = new TableColumn("Contact");
        tableColumn6.setCellValueFactory((Callback)new PropertyValueFactory("contactNumber"));
        TableColumn tableColumn7 = new TableColumn("Check-In");
        tableColumn7.setCellValueFactory((Callback)new PropertyValueFactory("checkInDate"));
        TableColumn tableColumn8 = new TableColumn("Check-Out");
        tableColumn8.setCellValueFactory((Callback)new PropertyValueFactory("expectedCheckOutDate"));
        tableView.getColumns().addAll((Object[])new TableColumn[]{tableColumn2, tableColumn3, tableColumn4, tableColumn5, tableColumn6, tableColumn7, tableColumn8});
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        ComboBox comboBox = new ComboBox(FXCollections.observableArrayList((Object[])new String[]{"All Rooms", "Available", "Occupied"}));
        comboBox.setValue((Object)"All Rooms");
        comboBox.setStyle("-fx-font-size: 13px; -fx-pref-width: 160;");
        comboBox.setOnAction(actionEvent -> filteredList.setPredicate(room -> ((String)comboBox.getValue()).equals("All Rooms") || room.getStatus().equals(comboBox.getValue())));
        Label label = new Label("Filter:");
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;");
        HBox hBox = new HBox(10.0, new Node[]{label, comboBox});
        hBox.setAlignment(Pos.CENTER_LEFT);
        TextField textField = new TextField();
        textField.setPromptText("Guest Name");
        this.styleTextField(textField);
        TextField textField2 = new TextField();
        textField2.setPromptText("Contact Number");
        this.styleTextField(textField2);
        Spinner spinner = new Spinner(1, 30, 1);
        spinner.setPrefWidth(100.0);
        spinner.setStyle("-fx-font-size: 13px;");
        Button button = this.createStyledButton("Book", "#1a5e3a");
        Button button2 = this.createStyledButton("Extend", "#7a5c1e");
        Button button3 = this.createStyledButton("Checkout", "#7a1e1e");
        button2.setDisable(true);
        button3.setDisable(true);
        GridPane gridPane = new GridPane();
        gridPane.setVgap(12.0);
        gridPane.setHgap(12.0);
        gridPane.setPadding(new Insets(15.0));
        Label label2 = new Label("Guest Name");
        label2.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;");
        Label label3 = new Label("Phone");
        label3.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;");
        Label label4 = new Label("Duration (Days)");
        label4.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;");
        gridPane.add((Node)label2, 0, 0);
        gridPane.add((Node)textField, 0, 1);
        gridPane.add((Node)label3, 0, 2);
        gridPane.add((Node)textField2, 0, 3);
        gridPane.add((Node)label4, 0, 4);
        gridPane.add((Node)spinner, 0, 5);
        HBox hBox2 = new HBox(10.0, new Node[]{button, button2, button3});
        hBox2.setPadding(new Insets(8.0, 0.0, 0.0, 0.0));
        gridPane.add((Node)hBox2, 0, 6);
        Label label5 = new Label("Action Desk");
        label5.setFont(Font.font((String)"Georgia", (FontWeight)FontWeight.BOLD, (double)16.0));
        label5.setStyle("-fx-text-fill: #2c2c2c;");
        Region region = new Region();
        region.setPrefHeight(2.0);
        region.setMaxWidth(Double.MAX_VALUE);
        region.setStyle("-fx-background-color: #c9a96e; -fx-opacity: 0.5;");
        VBox vBox = new VBox(10.0, new Node[]{label5, region, gridPane});
        vBox.setPadding(new Insets(20.0));
        vBox.setStyle("-fx-background-color: white;-fx-background-radius: 12;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.0, 0, 3);");
        vBox.setPrefWidth(320.0);
        tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, room, room2) -> {
            if (room2 != null) {
                if (room2.getStatus().equals("Occupied")) {
                    textField.setText(room2.getCustomerName());
                    textField2.setText(room2.getContactNumber());
                    textField.setEditable(false);
                    textField2.setEditable(false);
                    spinner.setDisable(true);
                    button.setDisable(true);
                    button2.setDisable(false);
                    button3.setDisable(false);
                } else {
                    textField.clear();
                    textField2.clear();
                    textField.setEditable(true);
                    textField2.setEditable(true);
                    spinner.setDisable(false);
                    button.setDisable(false);
                    button2.setDisable(true);
                    button3.setDisable(true);
                }
            }
        });
        button.setOnAction(actionEvent -> {
            Room room = (Room)tableView.getSelectionModel().getSelectedItem();
            if (room != null && !textField.getText().isEmpty()) {
                room.setStatus("Occupied");
                room.setOccupancy(textField.getText(), textField2.getText(), LocalDate.now(), LocalDate.now().plusDays(((Integer)spinner.getValue()).intValue()));
                tableView.refresh();
                this.updateDashboardMetrics();
                this.saveData();
            }
        });
        button2.setOnAction(actionEvent -> {
            Room room = (Room)tableView.getSelectionModel().getSelectedItem();
            if (room != null) {
                TextInputDialog textInputDialog = new TextInputDialog("1");
                textInputDialog.setTitle("Extend Stay");
                textInputDialog.setHeaderText("Additional Days:");
                textInputDialog.setContentText("Enter number of days:");
                textInputDialog.showAndWait().ifPresent(string -> {
                    room.extendStay(Integer.parseInt(string));
                    tableView.refresh();
                    this.saveData();
                });
            }
        });
        button3.setOnAction(actionEvent -> {
            Room room = (Room)tableView.getSelectionModel().getSelectedItem();
            if (room != null) {
                long l = ChronoUnit.DAYS.between(room.getCheckInDate(), LocalDate.now());
                if (l == 0L) {
                    l = 1L;
                }
                double d = (double)l * room.getPrice();
                double d2 = d * (this.gstRate / 100.0);
                double d3 = d + d2;
                this.historyList.add((Object)new HistoryRecord(room.getRoomNumber(), room.getCustomerName(), LocalDate.now().toString(), d3));
                String string = String.format("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557\n\u2551     MIT GRAND REGENCY            \u2551\n\u2551        FINAL INVOICE             \u2551\n\u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563\n\u2551 Guest: %-25s \u2551\n\u2551 Room:  %-25s \u2551\n\u2551 Days:  %-25d \u2551\n\u2551 Rate:  \u20b9%-24.2f \u2551\n\u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563\n\u2551 Subtotal:  \u20b9%-20.2f \u2551\n\u2551 GST %.1f%%:  \u20b9%-20.2f \u2551\n\u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563\n\u2551 GRAND TOTAL: \u20b9%-18.2f \u2551\n\u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d", room.getCustomerName(), room.getRoomNumber(), l, room.getPrice(), d, this.gstRate, d2, d3);
                room.clearOccupancy();
                tableView.refresh();
                this.updateDashboardMetrics();
                this.saveData();
                this.showAlert(Alert.AlertType.INFORMATION, "Checkout Complete", string);
            }
        });
        VBox vBox2 = new VBox(20.0, new Node[]{vBox});
        vBox2.setPadding(new Insets(0.0, 10.0, 10.0, 10.0));
        VBox vBox3 = new VBox(12.0, new Node[]{hBox, tableView});
        vBox3.setPadding(new Insets(0.0, 10.0, 10.0, 0.0));
        VBox.setVgrow((Node)tableView, (Priority)Priority.ALWAYS);
        HBox.setHgrow((Node)vBox3, (Priority)Priority.ALWAYS);
        HBox hBox3 = new HBox(15.0, new Node[]{vBox3, vBox2});
        HBox.setHgrow((Node)vBox3, (Priority)Priority.ALWAYS);
        VBox vBox4 = new VBox(20.0, new Node[]{this.createSectionHeader("Room Bookings", "Manage guest check-in, check-out, and stay extensions"), hBox3});
        vBox4.setPadding(new Insets(30.0));
        VBox.setVgrow((Node)hBox3, (Priority)Priority.ALWAYS);
        return vBox4;
    }

    private Node createInventoryView() {
        TableView tableView = new TableView();
        this.styleTable(tableView);
        tableView.setItems(this.roomList);
        TableColumn tableColumn = new TableColumn("Room #");
        tableColumn.setCellValueFactory((Callback)new PropertyValueFactory("roomNumber"));
        tableColumn.setStyle("-fx-font-weight: bold;");
        TableColumn tableColumn2 = new TableColumn("Type");
        tableColumn2.setCellValueFactory((Callback)new PropertyValueFactory("roomType"));
        TableColumn tableColumn3 = new TableColumn("Rate (\u20b9/Day)");
        tableColumn3.setCellValueFactory((Callback)new PropertyValueFactory("price"));
        TableColumn tableColumn4 = new TableColumn("Status");
        tableColumn4.setCellValueFactory((Callback)new PropertyValueFactory("status"));
        tableView.getColumns().addAll((Object[])new TableColumn[]{tableColumn, tableColumn2, tableColumn3, tableColumn4});
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TextField textField = new TextField();
        textField.setPromptText("e.g. 101");
        this.styleTextField(textField);
        textField.setPrefWidth(120.0);
        ComboBox comboBox = new ComboBox(FXCollections.observableArrayList((Object[])new String[]{"Single", "Double", "Deluxe"}));
        comboBox.setPromptText("Select Type");
        comboBox.setStyle("-fx-font-size: 13px;");
        Button button = this.createStyledButton("+ Add Room", ROYAL_BLUE);
        Button button2 = this.createStyledButton("- Remove", DANGER_RED);
        button2.setOnAction(actionEvent -> {
            Room room = (Room)tableView.getSelectionModel().getSelectedItem();
            if (room != null && room.getStatus().equals("Available")) {
                this.roomList.remove((Object)room);
                this.updateDashboardMetrics();
                this.saveData();
            } else {
                this.showAlert(Alert.AlertType.WARNING, "Cannot Remove", "Select an available (unoccupied) room to remove.");
            }
        });
        button.setOnAction(actionEvent -> {
            if (!textField.getText().isEmpty() && comboBox.getValue() != null) {
                String string = textField.getText();
                if (this.roomList.stream().anyMatch(room -> room.getRoomNumber().equals(string))) {
                    this.showAlert(Alert.AlertType.WARNING, "Duplicate Room", "Room " + string + " already exists.");
                    return;
                }
                double d = ((String)comboBox.getValue()).equals("Single") ? this.priceSingle : (((String)comboBox.getValue()).equals("Double") ? this.priceDouble : this.priceDeluxe);
                this.roomList.add((Object)new Room(string, (String)comboBox.getValue(), d, "Available", "", "", null, null));
                textField.clear();
                this.updateDashboardMetrics();
                this.saveData();
            }
        });
        Label label = new Label("Room No:");
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;");
        Label label2 = new Label("Type:");
        label2.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;");
        HBox hBox = new HBox(12.0, new Node[]{label, textField, label2, comboBox, button, button2});
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(15.0, 20.0, 15.0, 20.0));
        hBox.setStyle("-fx-background-color: white;-fx-background-radius: 12;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0.0, 0, 2);");
        VBox vBox = new VBox(20.0, new Node[]{this.createSectionHeader("Inventory Management", "Add, remove, and review all hotel rooms"), hBox, tableView});
        vBox.setPadding(new Insets(30.0));
        VBox.setVgrow((Node)tableView, (Priority)Priority.ALWAYS);
        return vBox;
    }

    private Node createHistoryView() {
        TableView tableView = new TableView();
        this.styleTable(tableView);
        tableView.setItems(this.historyList);
        TableColumn tableColumn = new TableColumn("Room #");
        tableColumn.setCellValueFactory((Callback)new PropertyValueFactory("roomNumber"));
        tableColumn.setStyle("-fx-font-weight: bold;");
        TableColumn tableColumn2 = new TableColumn("Guest Name");
        tableColumn2.setCellValueFactory((Callback)new PropertyValueFactory("guestName"));
        TableColumn tableColumn3 = new TableColumn("Checkout Date");
        tableColumn3.setCellValueFactory((Callback)new PropertyValueFactory("checkOut"));
        TableColumn tableColumn4 = new TableColumn("Amount Paid (\u20b9)");
        tableColumn4.setCellValueFactory((Callback)new PropertyValueFactory("totalPaid"));
        tableView.getColumns().addAll((Object[])new TableColumn[]{tableColumn, tableColumn2, tableColumn3, tableColumn4});
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        this.ledgerSummaryLabel = new Label();
        this.ledgerSummaryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;");
        double d = this.historyList.stream().mapToDouble(HistoryRecord::getTotalPaid).sum();
        this.ledgerSummaryLabel.setText(String.format("Total Revenue: \u20b9%.2f  |  Total Checkouts: %d", d, this.historyList.size()));
        this.historyList.addListener(change -> this.updateLedgerSummary());
        Label label = this.ledgerSummaryLabel;
        HBox hBox = new HBox(new Node[]{label});
        hBox.setPadding(new Insets(14.0, 20.0, 14.0, 20.0));
        hBox.setStyle("-fx-background-color: white;-fx-background-radius: 10;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0.0, 0, 2);");
        VBox vBox = new VBox(20.0, new Node[]{this.createSectionHeader("Financial Ledger", "Complete checkout history and revenue records"), hBox, tableView});
        vBox.setPadding(new Insets(30.0));
        VBox.setVgrow((Node)tableView, (Priority)Priority.ALWAYS);
        return vBox;
    }

    private Node createSettingsView() {
        TextField textField = new TextField(String.valueOf(this.priceSingle));
        this.styleTextField(textField);
        TextField textField2 = new TextField(String.valueOf(this.priceDouble));
        this.styleTextField(textField2);
        TextField textField3 = new TextField(String.valueOf(this.priceDeluxe));
        this.styleTextField(textField3);
        TextField textField4 = new TextField(String.valueOf(this.gstRate));
        this.styleTextField(textField4);
        Button button = this.createStyledButton("Save Configuration", GOLD_ACCENT);
        button.setStyle(button.getStyle() + "-fx-text-fill: #2c2c2c; -fx-font-weight: bold;");
        button.setOnAction(actionEvent -> {
            try {
                this.priceSingle = Double.parseDouble(textField.getText());
                this.priceDouble = Double.parseDouble(textField2.getText());
                this.priceDeluxe = Double.parseDouble(textField3.getText());
                this.gstRate = Double.parseDouble(textField4.getText());
                this.saveSettings();
                this.showAlert(Alert.AlertType.INFORMATION, "Settings Saved", "Global pricing and GST updated successfully.\nNote: Existing rooms retain their original locked prices.");
            }
            catch (Exception exception) {
                this.showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric values.");
            }
        });
        GridPane gridPane = new GridPane();
        gridPane.setVgap(18.0);
        gridPane.setHgap(20.0);
        gridPane.setPadding(new Insets(25.0));
        String string = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;";
        Label label = new Label("Single Room Rate (\u20b9):");
        label.setStyle(string);
        Label label2 = new Label("Double Room Rate (\u20b9):");
        label2.setStyle(string);
        Label label3 = new Label("Deluxe Room Rate (\u20b9):");
        label3.setStyle(string);
        Label label4 = new Label("Global GST Rate (%):");
        label4.setStyle(string);
        gridPane.add((Node)label, 0, 0);
        gridPane.add((Node)textField, 1, 0);
        gridPane.add((Node)label2, 0, 1);
        gridPane.add((Node)textField2, 1, 1);
        gridPane.add((Node)label3, 0, 2);
        gridPane.add((Node)textField3, 1, 2);
        gridPane.add((Node)label4, 0, 3);
        gridPane.add((Node)textField4, 1, 3);
        gridPane.add((Node)button, 1, 5);
        VBox vBox = new VBox(15.0, new Node[]{gridPane});
        vBox.setPadding(new Insets(10.0));
        vBox.setStyle("-fx-background-color: white;-fx-background-radius: 14;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.0, 0, 4);");
        vBox.setMaxWidth(500.0);
        VBox vBox2 = new VBox(25.0, new Node[]{this.createSectionHeader("System Configuration", "Manage room pricing and tax rates"), vBox});
        vBox2.setPadding(new Insets(35.0));
        return vBox2;
    }

    private Button createStyledButton(String string, String string2) {
        Button button = new Button(string);
        button.setStyle("-fx-background-color: " + string2 + ";-fx-text-fill: white;-fx-font-size: 13px;-fx-font-weight: bold;-fx-padding: 8 18;-fx-background-radius: 8;-fx-cursor: hand;");
        button.setOnMouseEntered(mouseEvent -> button.setStyle("-fx-background-color: derive(" + string2 + ", -15%);-fx-text-fill: white;-fx-font-size: 13px;-fx-font-weight: bold;-fx-padding: 8 18;-fx-background-radius: 8;-fx-cursor: hand;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.0, 0, 2);"));
        button.setOnMouseExited(mouseEvent -> button.setStyle("-fx-background-color: " + string2 + ";-fx-text-fill: white;-fx-font-size: 13px;-fx-font-weight: bold;-fx-padding: 8 18;-fx-background-radius: 8;-fx-cursor: hand;"));
        return button;
    }

    private void styleTextField(TextField textField) {
        textField.setStyle("-fx-font-size: 13px;-fx-padding: 8 12;-fx-background-radius: 8;-fx-border-color: #ddd;-fx-border-radius: 8;-fx-background-color: #fefefe;");
        textField.setOnMouseEntered(mouseEvent -> textField.setStyle("-fx-font-size: 13px;-fx-padding: 8 12;-fx-background-radius: 8;-fx-border-color: #c9a96e;-fx-border-radius: 8;-fx-background-color: white;"));
        textField.setOnMouseExited(mouseEvent -> {
            if (!textField.isFocused()) {
                textField.setStyle("-fx-font-size: 13px;-fx-padding: 8 12;-fx-background-radius: 8;-fx-border-color: #ddd;-fx-border-radius: 8;-fx-background-color: #fefefe;");
            }
        });
    }

    private <T> void styleTable(TableView<T> tableView) {
        tableView.setStyle("-fx-background-color: white;-fx-background-radius: 12;-fx-border-color: #e8e8e8;-fx-border-radius: 12;-fx-font-size: 13px;");
        tableView.setFixedCellSize(40.0);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private void loadSettings() {
        try (Connection connection = this.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT setting_key, setting_value FROM settings");){
            while (resultSet.next()) {
                String string = resultSet.getString("setting_key");
                String string2 = resultSet.getString("setting_value");
                switch (string) {
                    case "priceSingle": {
                        this.priceSingle = Double.parseDouble(string2);
                        break;
                    }
                    case "priceDouble": {
                        this.priceDouble = Double.parseDouble(string2);
                        break;
                    }
                    case "priceDeluxe": {
                        this.priceDeluxe = Double.parseDouble(string2);
                        break;
                    }
                    case "gstRate": {
                        this.gstRate = Double.parseDouble(string2);
                    }
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void saveSettings() {
        String string = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE setting_value = ?";
        try (Connection connection = this.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(string);){
            String[][] stringArrayArray;
            for (String[] stringArray : stringArrayArray = new String[][]{{"priceSingle", String.valueOf(this.priceSingle)}, {"priceDouble", String.valueOf(this.priceDouble)}, {"priceDeluxe", String.valueOf(this.priceDeluxe)}, {"gstRate", String.valueOf(this.gstRate)}}) {
                preparedStatement.setString(1, stringArray[0]);
                preparedStatement.setString(2, stringArray[1]);
                preparedStatement.setString(3, stringArray[1]);
                preparedStatement.executeUpdate();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void loadData() {
        try (Connection connection = this.getConnection();
             Statement statement = connection.createStatement();){
            ResultSet resultSet;
            ResultSet resultSet2 = statement.executeQuery("SELECT * FROM rooms");
            while (resultSet2.next()) {
                resultSet = resultSet2.getDate("check_in_date") != null ? resultSet2.getDate("check_in_date").toLocalDate() : null;
                LocalDate localDate = resultSet2.getDate("expected_checkout_date") != null ? resultSet2.getDate("expected_checkout_date").toLocalDate() : null;
                this.roomList.add((Object)new Room(resultSet2.getString("room_number"), resultSet2.getString("room_type"), resultSet2.getDouble("price"), resultSet2.getString("status"), resultSet2.getString("customer_name"), resultSet2.getString("contact_number"), (LocalDate)((Object)resultSet), localDate));
            }
            resultSet = statement.executeQuery("SELECT room_number, guest_name, checkout_date, total_paid FROM checkout_history");
            while (resultSet.next()) {
                this.historyList.add((Object)new HistoryRecord(resultSet.getString("room_number"), resultSet.getString("guest_name"), resultSet.getString("checkout_date"), resultSet.getDouble("total_paid")));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void saveData() {
        try (Connection connection = this.getConnection();){
            connection.setAutoCommit(false);
            try (Object object = connection.createStatement();){
                object.executeUpdate("DELETE FROM rooms");
            }
            object = "REPLACE INTO rooms (room_number, room_type, price, status, customer_name, contact_number, check_in_date, expected_checkout_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Object object = connection.prepareStatement((String)object);){
                for (Object object2 : this.roomList) {
                    object.setString(1, ((Room)object2).getRoomNumber());
                    object.setString(2, ((Room)object2).getRoomType());
                    object.setDouble(3, ((Room)object2).getPrice());
                    object.setString(4, ((Room)object2).getStatus());
                    object.setString(5, ((Room)object2).getCustomerName());
                    object.setString(6, ((Room)object2).getContactNumber());
                    object.setDate(7, ((Room)object2).getCheckInDate() != null ? Date.valueOf(((Room)object2).getCheckInDate()) : null);
                    object.setDate(8, ((Room)object2).getExpectedCheckOutDate() != null ? Date.valueOf(((Room)object2).getExpectedCheckOutDate()) : null);
                    object.executeUpdate();
                }
            }
            object = connection.createStatement();
            try {
                object.executeUpdate("DELETE FROM checkout_history");
            }
            finally {
                if (object != null) {
                    object.close();
                }
            }
            object = "REPLACE INTO checkout_history (id, room_number, guest_name, checkout_date, total_paid) VALUES (NULL, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement((String)object);){
                for (HistoryRecord historyRecord : this.historyList) {
                    preparedStatement.setString(1, historyRecord.getRoomNumber());
                    preparedStatement.setString(2, historyRecord.getGuestName());
                    preparedStatement.setString(3, historyRecord.getCheckOut());
                    preparedStatement.setDouble(4, historyRecord.getTotalPaid());
                    preparedStatement.executeUpdate();
                }
            }
            connection.commit();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String string, String string2) {
        Alert alert = new Alert(alertType);
        alert.setTitle(string);
        alert.setHeaderText(null);
        alert.setContentText(string2);
        alert.getDialogPane().setStyle("-fx-font-family: monospace;-fx-font-size: 13px;");
        alert.getDialogPane().setMinWidth(450.0);
        alert.showAndWait();
    }

    public static void main(String[] stringArray) {
        HotelManagement.launch((String[])stringArray);
    }

    public static class Room {
        private final StringProperty roomNumber = new SimpleStringProperty();
        private final StringProperty roomType = new SimpleStringProperty();
        private final DoubleProperty price = new SimpleDoubleProperty();
        private final StringProperty status = new SimpleStringProperty();
        private final StringProperty customerName = new SimpleStringProperty();
        private final StringProperty contactNumber = new SimpleStringProperty();
        private final ObjectProperty<LocalDate> checkInDate = new SimpleObjectProperty();
        private final ObjectProperty<LocalDate> expectedCheckOutDate = new SimpleObjectProperty();

        public Room(String string, String string2, double d, String string3, String string4, String string5, LocalDate localDate, LocalDate localDate2) {
            this.roomNumber.set((Object)string);
            this.roomType.set((Object)string2);
            this.price.set(d);
            this.status.set((Object)string3);
            this.customerName.set((Object)string4);
            this.contactNumber.set((Object)string5);
            this.checkInDate.set((Object)localDate);
            this.expectedCheckOutDate.set((Object)localDate2);
        }

        public String getRoomNumber() {
            return (String)this.roomNumber.get();
        }

        public StringProperty roomNumberProperty() {
            return this.roomNumber;
        }

        public String getRoomType() {
            return (String)this.roomType.get();
        }

        public StringProperty roomTypeProperty() {
            return this.roomType;
        }

        public double getPrice() {
            return this.price.get();
        }

        public DoubleProperty priceProperty() {
            return this.price;
        }

        public String getStatus() {
            return (String)this.status.get();
        }

        public StringProperty statusProperty() {
            return this.status;
        }

        public String getCustomerName() {
            return (String)this.customerName.get();
        }

        public StringProperty customerNameProperty() {
            return this.customerName;
        }

        public String getContactNumber() {
            return (String)this.contactNumber.get();
        }

        public StringProperty contactNumberProperty() {
            return this.contactNumber;
        }

        public LocalDate getExpectedCheckOutDate() {
            return (LocalDate)this.expectedCheckOutDate.get();
        }

        public ObjectProperty<LocalDate> expectedCheckOutDateProperty() {
            return this.expectedCheckOutDate;
        }

        public LocalDate getCheckInDate() {
            return (LocalDate)this.checkInDate.get();
        }

        public ObjectProperty<LocalDate> checkInDateProperty() {
            return this.checkInDate;
        }

        public void setStatus(String string) {
            this.status.set((Object)string);
        }

        public void setOccupancy(String string, String string2, LocalDate localDate, LocalDate localDate2) {
            this.customerName.set((Object)string);
            this.contactNumber.set((Object)string2);
            this.checkInDate.set((Object)localDate);
            this.expectedCheckOutDate.set((Object)localDate2);
        }

        public void extendStay(int n) {
            if (this.expectedCheckOutDate.get() != null) {
                this.expectedCheckOutDate.set((Object)((LocalDate)this.expectedCheckOutDate.get()).plusDays(n));
            }
        }

        public void clearOccupancy() {
            this.customerName.set((Object)"");
            this.contactNumber.set((Object)"");
            this.checkInDate.set(null);
            this.expectedCheckOutDate.set(null);
            this.status.set((Object)"Available");
        }

        public String toCSV() {
            return String.join((CharSequence)",", this.getRoomNumber(), this.getRoomType(), String.valueOf(this.getPrice()), this.getStatus(), this.getCustomerName(), this.getContactNumber(), this.checkInDate.get() != null ? ((LocalDate)this.checkInDate.get()).toString() : "null", this.expectedCheckOutDate.get() != null ? ((LocalDate)this.expectedCheckOutDate.get()).toString() : "null");
        }
    }

    public static class HistoryRecord {
        private final StringProperty roomNumber = new SimpleStringProperty();
        private final StringProperty guestName = new SimpleStringProperty();
        private final StringProperty checkOut = new SimpleStringProperty();
        private final DoubleProperty totalPaid = new SimpleDoubleProperty();

        public HistoryRecord(String string, String string2, String string3, double d) {
            this.roomNumber.set((Object)string);
            this.guestName.set((Object)string2);
            this.checkOut.set((Object)string3);
            this.totalPaid.set(d);
        }

        public String getRoomNumber() {
            return (String)this.roomNumber.get();
        }

        public StringProperty roomNumberProperty() {
            return this.roomNumber;
        }

        public String getGuestName() {
            return (String)this.guestName.get();
        }

        public StringProperty guestNameProperty() {
            return this.guestName;
        }

        public String getCheckOut() {
            return (String)this.checkOut.get();
        }

        public StringProperty checkOutProperty() {
            return this.checkOut;
        }

        public double getTotalPaid() {
            return this.totalPaid.get();
        }

        public DoubleProperty totalPaidProperty() {
            return this.totalPaid;
        }

        public String toCSV() {
            return String.join((CharSequence)",", this.getRoomNumber(), this.getGuestName(), this.getCheckOut(), String.valueOf(this.getTotalPaid()));
        }
    }
}

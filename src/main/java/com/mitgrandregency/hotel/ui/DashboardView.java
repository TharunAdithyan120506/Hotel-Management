package com.mitgrandregency.hotel.ui;

import com.mitgrandregency.hotel.model.AppState;
import com.mitgrandregency.hotel.model.Room;
import com.mitgrandregency.hotel.service.AadhaarStorageService;
import com.mitgrandregency.hotel.service.ReportService;

import javafx.animation.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard panel — live overview with stat cards and bento-box room tiles.
 */
public class DashboardView {

    private final AppState state;
    private final ReportService reportService;
    private final AadhaarStorageService aadhaarService;

    private final Label lblTotal = new Label("0");
    private final Label lblAvail = new Label("0");
    private final Label lblOcc = new Label("0");
    private final Label lblClean = new Label("0");
    private final Label lblRevenue = new Label("\u20B90");
    private VBox revenueCard;
    private FlowPane activityCenter;

    public DashboardView(AppState state, ReportService reportService, AadhaarStorageService aadhaarService) {
        this.state = state;
        this.reportService = reportService;
        this.aadhaarService = aadhaarService;
    }

    public VBox getRevenueCard() { return revenueCard; }

    public Node createView() {
        revenueCard = UIUtils.createCard("Revenue", lblRevenue, "#2a1a3c", "#3d2452", "\u2605");

        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER_LEFT);
        cards.getChildren().addAll(
                UIUtils.createCard("Total Rooms", lblTotal, "#1a1a2e", "#16213e", "\u2302"),
                UIUtils.createCard("Available", lblAvail, "#1a3c34", "#1e5245", "\u2714"),
                UIUtils.createCard("Occupied", lblOcc, "#3c1a1a", "#522020", "\u263A"),
                UIUtils.createCard("Cleaning", lblClean, "#1a2a3c", "#1e3048", "\u2728"),
                revenueCard);

        // Activity Center header
        Label actHeader = new Label("Activity Center \u2014 Live Occupancy");
        actHeader.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        actHeader.setStyle("-fx-text-fill: " + UIUtils.DARK_TEXT + ";");

        Label actSub = new Label("Rooms currently in use with guest details");
        actSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");

        Region actUnderline = new Region();
        actUnderline.setPrefHeight(2);
        actUnderline.setPrefWidth(50);
        actUnderline.setMaxWidth(50);
        actUnderline.setStyle("-fx-background-color: " + UIUtils.GOLD_ACCENT + "; -fx-background-radius: 2;");

        VBox actHeaderBox = new VBox(4, actHeader, actSub, actUnderline);

        // Filter Bar
        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup filterGroup = new ToggleGroup();
        String[] filters = { "All", "Available", "Occupied", "Cleaning", "Urgent" };
        for (String f : filters) {
            ToggleButton tb = new ToggleButton(f);
            tb.setToggleGroup(filterGroup);
            tb.setStyle("-fx-background-color: transparent; -fx-border-color: #ccc; "
                    + "-fx-border-radius: 15; -fx-background-radius: 15; -fx-text-fill: #555; "
                    + "-fx-font-weight: bold; -fx-padding: 4 12;");

            tb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    tb.setStyle("-fx-background-color: " + UIUtils.GOLD_ACCENT + "; "
                            + "-fx-border-color: " + UIUtils.GOLD_ACCENT + "; -fx-border-radius: 15; "
                            + "-fx-background-radius: 15; -fx-text-fill: " + UIUtils.DARK_TEXT + "; "
                            + "-fx-font-weight: bold; -fx-padding: 4 12;");
                    refreshActivityCenter(f);
                } else {
                    tb.setStyle("-fx-background-color: transparent; -fx-border-color: #ccc; "
                            + "-fx-border-radius: 15; -fx-background-radius: 15; -fx-text-fill: #555; "
                            + "-fx-font-weight: bold; -fx-padding: 4 12;");
                }
            });
            if ("All".equals(f)) tb.setSelected(true);
            filterBar.getChildren().add(tb);
        }

        filterGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) oldToggle.setSelected(true);
        });

        VBox actHeaderAndFilterBox = new VBox(10, actHeaderBox, filterBar);

        activityCenter = new FlowPane();
        activityCenter.setHgap(14);
        activityCenter.setVgap(14);
        activityCenter.setPadding(new Insets(5));
        refreshActivityCenter("All");

        ScrollPane scrollPane = new ScrollPane(activityCenter);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; "
                + "-fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox activityCard = new VBox(12, actHeaderAndFilterBox, scrollPane);
        activityCard.setPadding(new Insets(20));
        activityCard.setStyle("-fx-background-color: #f5f2ee; -fx-background-radius: 14;");
        VBox.setVgrow(activityCard, Priority.ALWAYS);

        VBox root = new VBox(22,
                UIUtils.createSectionHeader("Live Overview",
                        "Real-time hotel occupancy and revenue at a glance"),
                cards, activityCard);
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

    public void updateMetrics() {
        lblTotal.setText(String.valueOf(reportService.totalRooms()));
        lblAvail.setText(String.valueOf(reportService.countByStatus("Available")));
        lblOcc.setText(String.valueOf(reportService.countByStatus("Occupied")));
        lblClean.setText(String.valueOf(reportService.countByStatus("Cleaning")));
        lblRevenue.setText(String.format("\u20B9%.0f", reportService.calculateTotalRevenue()));
        refreshActivityCenter("All");
    }

    private void refreshActivityCenter(String filter) {
        if (activityCenter == null) return;
        activityCenter.getChildren().clear();

        List<Room> filteredRooms;
        if ("All".equals(filter)) {
            filteredRooms = new ArrayList<>(state.getRoomList());
        } else if ("Urgent".equals(filter)) {
            filteredRooms = state.getRoomList().stream()
                    .filter(r -> "Urgent Cleaning".equals(r.getStatus()))
                    .collect(Collectors.toList());
        } else {
            filteredRooms = state.getRoomList().stream()
                    .filter(r -> filter.equals(r.getStatus()))
                    .collect(Collectors.toList());
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

    private VBox createRoomTile(Room room) {
        Label roomNo = new Label("Room " + room.getRoomNumber());
        roomNo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UIUtils.DARK_TEXT + ";");

        Label roomType = new Label(room.getRoomType());
        roomType.setStyle("-fx-font-size: 11px; -fx-text-fill: " + UIUtils.GOLD_ACCENT + "; -fx-font-weight: bold;");

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
            availBadge.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 10;");
            tile.getChildren().addAll(topAccent, roomNo, roomType, rateLabel, guestLabel, availBadge);

        } else if ("Occupied".equals(room.getStatus())) {
            topAccent.setStyle("-fx-background-color: " + UIUtils.GOLD_ACCENT + "; -fx-background-radius: 3 3 0 0;");
            Label guestLabel = new Label("\u263A " + room.getCustomerName());
            guestLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            Label contactLabel = new Label("\u260E " + room.getContactNumber());
            contactLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

            String checkoutStr = room.getExpectedCheckOutDate() != null
                    ? room.getExpectedCheckOutDate().toString() : "N/A";
            Label checkoutLabel = new Label("Checkout: " + checkoutStr);
            checkoutLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

            String daysText = "";
            if (room.getExpectedCheckOutDate() != null) {
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), room.getExpectedCheckOutDate());
                if (daysLeft <= 0) {
                    daysText = "OVERDUE";
                    checkoutLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + UIUtils.DANGER_RED
                            + "; -fx-font-weight: bold;");
                } else if (daysLeft == 1) {
                    daysText = "1 day left";
                } else {
                    daysText = daysLeft + " days left";
                }
            }
            Label daysLabel = new Label(daysText);
            daysLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + UIUtils.GOLD_ACCENT
                    + "; -fx-font-weight: bold;");

            tile.getChildren().addAll(topAccent, roomNo, roomType, guestLabel, contactLabel, checkoutLabel, daysLabel);

            Image aadhaarImg = aadhaarService.loadAadhaarImage(room.getAadhaarPath());
            if (aadhaarImg != null) {
                ImageView aadhaarThumb = new ImageView(aadhaarImg);
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
            Label urgentLabel = new Label("\u26A0 URGENT");
            urgentLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 10;");
            tile.getChildren().addAll(topAccent, roomNo, roomType, urgentLabel);
        } else {
            topAccent.setStyle("-fx-background-color: #aaaaaa; -fx-background-radius: 3 3 0 0;");
            tile.getChildren().addAll(topAccent, roomNo, roomType);
        }

        tile.setPadding(new Insets(0, 14, 12, 14));
        tile.setPrefWidth(195);
        tile.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0.0, 0, 3);");
        return tile;
    }
}

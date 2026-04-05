package com.mitgrandregency.hotel.ui;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Shared UI helper methods and brand constants.
 * All color constants, styled button factories, section headers,
 * and common helper methods live here.
 */
public final class UIUtils {

    private UIUtils() { }

    // --- Brand Colors ---
    public static final String SIDEBAR_TOP    = "#1a1a2e";
    public static final String SIDEBAR_BOTTOM = "#16213e";
    public static final String GOLD_ACCENT    = "#c9a96e";
    public static final String GOLD_LIGHT     = "#e8d5b5";
    public static final String IVORY_TEXT     = "#f0e6d3";
    public static final String BG_WARM        = "#faf8f5";
    public static final String CARD_SHADOW    = "#00000033";
    public static final String DARK_TEXT      = "#2c2c2c";
    public static final String SUCCESS_GREEN  = "#2ecc71";
    public static final String DANGER_RED     = "#e74c3c";
    public static final String AMBER          = "#f39c12";
    public static final String ROYAL_BLUE     = "#2980b9";

    /**
     * Creates a sidebar navigation button with hover animation.
     *
     * @param text         button label including emoji
     * @param activeHolder single-element array holding the currently active button
     * @return a StackPane wrapper whose userData is the Button
     */
    public static StackPane createNavButton(String text, Button[] activeHolder) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefHeight(42);
        applyNavStyle(btn, false);

        StackPane wrapper = new StackPane(btn);

        TranslateTransition tt = new TranslateTransition(Duration.millis(150), btn);

        wrapper.setOnMouseEntered(e -> {
            if (btn != activeHolder[0]) {
                tt.stop();
                tt.setFromX(btn.getTranslateX());
                tt.setToX(6);
                tt.play();
                btn.setStyle(
                    "-fx-background-color: rgba(201,169,110,0.15);"
                    + "-fx-text-fill: " + GOLD_ACCENT + ";"
                    + "-fx-font-size: 14px;"
                    + "-fx-alignment: center-left;"
                    + "-fx-padding: 10 20;"
                    + "-fx-background-radius: 8;"
                    + "-fx-cursor: hand;");
            }
        });
        wrapper.setOnMouseExited(e -> {
            if (btn != activeHolder[0]) {
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

    /**
     * Applies active or inactive style to a nav button.
     */
    public static void applyNavStyle(Button btn, boolean active) {
        if (active) {
            btn.setStyle(
                "-fx-background-color: rgba(201,169,110,0.25);"
                + "-fx-text-fill: " + GOLD_ACCENT + ";"
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
                + "-fx-alignment: center-left;"
                + "-fx-padding: 10 20;"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: transparent transparent transparent " + GOLD_ACCENT + ";"
                + "-fx-border-width: 0 0 0 3;"
                + "-fx-border-radius: 8;");
        } else {
            btn.setStyle(
                "-fx-background-color: transparent;"
                + "-fx-text-fill: " + IVORY_TEXT + ";"
                + "-fx-font-size: 14px;"
                + "-fx-alignment: center-left;"
                + "-fx-padding: 10 20;"
                + "-fx-background-radius: 8;"
                + "-fx-cursor: hand;");
        }
    }

    /**
     * Animates active nav selection.
     */
    public static void setActiveNav(Button btn, Button[] activeHolder) {
        if (activeHolder[0] != null) {
            applyNavStyle(activeHolder[0], false);
            ScaleTransition st1 = new ScaleTransition(Duration.millis(100),
                    (StackPane) activeHolder[0].getParent());
            st1.setToX(1.0);
            st1.setToY(1.0);
            st1.play();
        }
        activeHolder[0] = btn;
        applyNavStyle(btn, true);
        ScaleTransition st2 = new ScaleTransition(Duration.millis(100),
                (StackPane) btn.getParent());
        st2.setToX(1.02);
        st2.setToY(1.02);
        st2.play();
    }

    /**
     * Creates a section header with title, subtitle, and gold underline.
     */
    public static VBox createSectionHeader(String title, String subtitle) {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 10, 0));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
        titleLabel.setStyle("-fx-text-fill: " + DARK_TEXT + ";");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subtitleLabel.setStyle("-fx-text-fill: #888888;");

        Region underline = new Region();
        underline.setPrefHeight(3);
        underline.setPrefWidth(60);
        underline.setMaxWidth(60);
        underline.setStyle("-fx-background-color: " + GOLD_ACCENT + "; -fx-background-radius: 2;");

        header.getChildren().addAll(titleLabel, subtitleLabel, underline);
        return header;
    }

    /**
     * Creates a dashboard stat card.
     */
    public static VBox createCard(String title, Label val,
                                  String gradientStart, String gradientEnd, String icon) {
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 38px;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle(
            "-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 12px; "
            + "-fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

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
            "-fx-background-color: linear-gradient(to bottom right, "
            + gradientStart + ", " + gradientEnd + ");"
            + "-fx-background-radius: 14;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0.0, 0, 4);");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    /**
     * Creates a styled action button with hover effect.
     */
    public static Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        String baseStyle = "-fx-background-color: " + color + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 13px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 8 18;"
                + "-fx-background-radius: 8;"
                + "-fx-cursor: hand;";
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: derive(" + color + ", -15%);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 13px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 8 18;"
                + "-fx-background-radius: 8;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.0, 0, 2);"));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }

    /**
     * Applies standard text-field styling (handled by CSS, this is a no-op hook).
     */
    public static void styleTextField(TextField tf) {
        // Styling defined in styles.css targeting .text-field
    }

    /**
     * Applies standard table styling (handled by CSS).
     */
    public static <T> void styleTable(TableView<T> table) {
        table.getStyleClass().add("table-view");
        table.setFixedCellSize(40);
    }

    /**
     * Shows a styled alert dialog.
     */
    public static void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.getDialogPane().setStyle(
                "-fx-font-family: monospace; -fx-font-size: 13px;");
        a.getDialogPane().setMinWidth(450);
        a.showAndWait();
    }
}

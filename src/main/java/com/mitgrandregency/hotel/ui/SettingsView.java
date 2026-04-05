package com.mitgrandregency.hotel.ui;

import com.mitgrandregency.hotel.dao.SettingsDAO;
import com.mitgrandregency.hotel.model.AppState;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class SettingsView {

    private final AppState state;
    private final SettingsDAO settingsDAO;

    public SettingsView(AppState state, SettingsDAO settingsDAO) {
        this.state = state;
        this.settingsDAO = settingsDAO;
    }

    public Node createView() {
        TextField tSin = new TextField(String.valueOf(state.getPriceSingle())); UIUtils.styleTextField(tSin);
        TextField tDou = new TextField(String.valueOf(state.getPriceDouble())); UIUtils.styleTextField(tDou);
        TextField tDel = new TextField(String.valueOf(state.getPriceDeluxe())); UIUtils.styleTextField(tDel);
        TextField tGst = new TextField(String.valueOf(state.getGstRate()));     UIUtils.styleTextField(tGst);

        Button btnSave = UIUtils.createStyledButton("Save Configuration", UIUtils.GOLD_ACCENT);
        btnSave.setStyle(btnSave.getStyle() + "-fx-text-fill:" + UIUtils.DARK_TEXT + ";-fx-font-weight:bold;");

        btnSave.setOnAction(e -> {
            try {
                state.setPriceSingle(Double.parseDouble(tSin.getText()));
                state.setPriceDouble(Double.parseDouble(tDou.getText()));
                state.setPriceDeluxe(Double.parseDouble(tDel.getText()));
                state.setGstRate(Double.parseDouble(tGst.getText()));
                settingsDAO.saveSettings(state);
                UIUtils.showAlert(Alert.AlertType.INFORMATION, "Settings Saved",
                        "Global pricing and GST updated successfully.\nNote: Existing rooms retain their original locked prices.");
            } catch (Exception ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric values.");
            }
        });

        String ls = "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.DARK_TEXT + ";";
        GridPane grid = new GridPane();
        grid.setVgap(18); grid.setHgap(20); grid.setPadding(new Insets(25));
        grid.add(new Label("Single Room Rate (\u20B9):") {{ setStyle(ls); }}, 0, 0); grid.add(tSin, 1, 0);
        grid.add(new Label("Double Room Rate (\u20B9):") {{ setStyle(ls); }}, 0, 1); grid.add(tDou, 1, 1);
        grid.add(new Label("Deluxe Room Rate (\u20B9):") {{ setStyle(ls); }}, 0, 2); grid.add(tDel, 1, 2);
        grid.add(new Label("Global GST Rate (%):") {{ setStyle(ls); }}, 0, 3); grid.add(tGst, 1, 3);
        grid.add(btnSave, 1, 5);

        VBox settingsCard = new VBox(15, grid);
        settingsCard.setPadding(new Insets(10));
        settingsCard.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),15,0.0,0,4);");
        settingsCard.setMaxWidth(500);

        VBox root = new VBox(25, UIUtils.createSectionHeader("System Configuration","Manage room pricing and tax rates"), settingsCard);
        root.setPadding(new Insets(35));
        return root;
    }
}

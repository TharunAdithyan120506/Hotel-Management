package com.mitgrandregency.hotel.ui;

import com.mitgrandregency.hotel.dao.RoomDAO;
import com.mitgrandregency.hotel.model.AppState;
import com.mitgrandregency.hotel.model.Room;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Inventory management panel — add, remove, import rooms.
 */
public class InventoryView {

    private final AppState state;
    private final RoomDAO roomDAO;
    private final Runnable onDataChanged;

    public InventoryView(AppState state, RoomDAO roomDAO, Runnable onDataChanged) {
        this.state = state;
        this.roomDAO = roomDAO;
        this.onDataChanged = onDataChanged;
    }

    @SuppressWarnings("unchecked")
    public Node createView() {
        TableView<Room> table = new TableView<>();
        UIUtils.styleTable(table);
        table.setItems(state.getRoomList());

        TableColumn<Room, String> cNum = new TableColumn<>("Room #");
        cNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        cNum.setStyle("-fx-font-weight: bold;");

        TableColumn<Room, String> cType = new TableColumn<>("Type");
        cType.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<Room, Double> cPrice = new TableColumn<>("Rate (\u20B9/Day)");
        cPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Room, String> cStatus = new TableColumn<>("Status");
        cStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(cNum, cType, cPrice, cStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TextField txtNo = new TextField();
        txtNo.setPromptText("e.g. 101");
        UIUtils.styleTextField(txtNo);
        txtNo.setPrefWidth(120);

        ComboBox<String> comboType = new ComboBox<>(
                FXCollections.observableArrayList("Single", "Double", "Deluxe"));
        comboType.setPromptText("Select Type");
        comboType.setStyle("-fx-font-size: 13px;");

        Button btnAdd = UIUtils.createStyledButton("+ Add Room", UIUtils.ROYAL_BLUE);
        btnAdd.setTooltip(new Tooltip("Add a new room to inventory"));
        Button btnRemove = UIUtils.createStyledButton("- Remove", UIUtils.DANGER_RED);
        btnRemove.setTooltip(new Tooltip("Remove the selected available room"));
        Button btnImport = UIUtils.createStyledButton("\u2B06 Import CSV", UIUtils.ROYAL_BLUE);
        btnImport.setTooltip(new Tooltip("Import rooms from a CSV file"));

        btnAdd.setOnAction(e -> {
            if (!txtNo.getText().isEmpty() && comboType.getValue() != null) {
                String newNo = txtNo.getText();
                if (state.getRoomList().stream().anyMatch(r -> r.getRoomNumber().equals(newNo))) {
                    UIUtils.showAlert(Alert.AlertType.WARNING, "Duplicate Room",
                            "Room " + newNo + " already exists.");
                    return;
                }
                double p = state.getPriceForType(comboType.getValue());
                state.getRoomList().add(new Room(newNo, comboType.getValue(), p));
                txtNo.clear();
                onDataChanged.run();
            }
        });

        btnRemove.setOnAction(e -> {
            Room selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && "Available".equals(selected.getStatus())) {
                state.getRoomList().remove(selected);
                roomDAO.delete(selected.getRoomNumber());
                onDataChanged.run();
            } else {
                UIUtils.showAlert(Alert.AlertType.WARNING, "Cannot Remove",
                        "Select an available (unoccupied) room to remove.");
            }
        });

        btnImport.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fc.showOpenDialog(null);
            if (file != null) {
                int added = 0, skipped = 0;
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line = br.readLine(); // skip header
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(",", -1);
                        if (parts.length < 1) { skipped++; continue; }
                        String rn = parts[0].trim();
                        if (rn.isEmpty() || state.getRoomList().stream()
                                .anyMatch(r -> r.getRoomNumber().equals(rn))) {
                            skipped++; continue;
                        }
                        String rt = parts.length > 1 ? parts[1].trim() : "";
                        if (!"Single".equals(rt) && !"Double".equals(rt) && !"Deluxe".equals(rt)) {
                            skipped++; continue;
                        }
                        double p = 0;
                        if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                            try { p = Double.parseDouble(parts[2].trim()); }
                            catch (NumberFormatException ex) { }
                        }
                        if (p <= 0) p = state.getPriceForType(rt);
                        String st = parts.length > 3 ? parts[3].trim() : "";
                        if (!"Available".equals(st) && !"Occupied".equals(st)) st = "Available";
                        Room newRoom = new Room(rn, rt, p);
                        newRoom.setStatus(st);
                        state.getRoomList().add(newRoom);
                        added++;
                    }
                    onDataChanged.run();
                    UIUtils.showAlert(Alert.AlertType.INFORMATION, "Import Complete",
                            "Import Complete \u2014 " + added + " rooms added, " + skipped + " rows skipped.");
                } catch (IOException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Import Error",
                            "Failed to read file. Please ensure it is a valid UTF-8 CSV.");
                }
            }
        });

        Label lRoom = new Label("Room No:");
        lRoom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + UIUtils.DARK_TEXT + ";");
        Label lType = new Label("Type:");
        lType.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + UIUtils.DARK_TEXT + ";");

        HBox form = new HBox(12, lRoom, txtNo, lType, comboType, btnAdd, btnRemove, btnImport);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(15, 20, 15, 20));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0.0, 0, 2);");

        VBox root = new VBox(20,
                UIUtils.createSectionHeader("Inventory Management",
                        "Add, remove, and review all hotel rooms"),
                form, table);
        root.setPadding(new Insets(30));
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }
}

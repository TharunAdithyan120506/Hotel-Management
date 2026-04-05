package com.mitgrandregency.hotel.ui;

import com.mitgrandregency.hotel.dao.MenuDAO;
import com.mitgrandregency.hotel.dao.OrderDAO;
import com.mitgrandregency.hotel.model.AppState;
import com.mitgrandregency.hotel.model.MenuItem;
import com.mitgrandregency.hotel.model.RestaurantOrder;
import com.mitgrandregency.hotel.model.Room;

import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dining & POS panel — menu management, ordering, and pending orders.
 */
public class RestaurantView {

    private final AppState state;
    private final MenuDAO menuDAO;
    private final OrderDAO orderDAO;
    private FilteredList<Room> occupiedRoomsForRestaurant;

    public RestaurantView(AppState state, MenuDAO menuDAO, OrderDAO orderDAO) {
        this.state = state;
        this.menuDAO = menuDAO;
        this.orderDAO = orderDAO;
    }

    public FilteredList<Room> getOccupiedRoomsFilter() {
        return occupiedRoomsForRestaurant;
    }

    @SuppressWarnings("unchecked")
    public Node createView() {
        TableView<MenuItem> menuTable = new TableView<>();
        UIUtils.styleTable(menuTable);
        menuTable.setItems(state.getMenuItemList());

        TableColumn<MenuItem, String> cName = new TableColumn<>("Item");
        cName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        TableColumn<MenuItem, Double> cPrice = new TableColumn<>("Price (\u20B9)");
        cPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        menuTable.getColumns().addAll(cName, cPrice);
        menuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableView<RestaurantOrder> orderTable = new TableView<>();
        UIUtils.styleTable(orderTable);
        orderTable.setItems(state.getRestaurantOrderList().filtered(o -> !o.isSettled()));
        state.getRestaurantOrderList().addListener(
                (ListChangeListener<RestaurantOrder>) c -> orderTable.refresh());

        TableColumn<RestaurantOrder, String> oRoom = new TableColumn<>("Room");
        oRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        TableColumn<RestaurantOrder, String> oItem = new TableColumn<>("Item");
        oItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        TableColumn<RestaurantOrder, String> oCat = new TableColumn<>("Category");
        oCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<RestaurantOrder, Integer> oQty = new TableColumn<>("Qty");
        oQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<RestaurantOrder, Double> oTotal = new TableColumn<>("Total (\u20B9)");
        oTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        TableColumn<RestaurantOrder, LocalDateTime> oTime = new TableColumn<>("Time");
        oTime.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        oTime.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null
                        : v.format(DateTimeFormatter.ofPattern("HH:mm, dd MMM")));
            }
        });

        orderTable.getColumns().addAll(oRoom, oItem, oCat, oQty, oTotal, oTime);
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ComboBox<Room> roomCombo = new ComboBox<>();
        roomCombo.setPromptText("Select Room");
        occupiedRoomsForRestaurant = new FilteredList<>(state.getRoomList(),
                r -> "Occupied".equals(r.getStatus()));
        roomCombo.setItems(occupiedRoomsForRestaurant);
        roomCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Room r) {
                return r == null ? "" : "Room " + r.getRoomNumber() + " \u2014 " + r.getCustomerName();
            }
            @Override
            public Room fromString(String s) { return null; }
        });

        state.getRoomList().addListener((ListChangeListener<Room>) change -> {
            occupiedRoomsForRestaurant.setPredicate(r -> "Occupied".equals(r.getStatus()));
            roomCombo.setItems(null);
            roomCombo.setItems(occupiedRoomsForRestaurant);
        });

        Spinner<Integer> qtySpinner = new Spinner<>(1, 20, 1);
        qtySpinner.setPrefWidth(80);

        Button btnOrder = UIUtils.createStyledButton("Place Order", "#2980b9");
        btnOrder.setOnAction(e -> {
            MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
            Room selectedRoom = roomCombo.getValue();
            if (selectedItem != null && selectedRoom != null) {
                int qty = qtySpinner.getValue();
                double total = qty * selectedItem.getUnitPrice();
                RestaurantOrder order = new RestaurantOrder(0,
                        selectedRoom.getRoomNumber(), selectedRoom.getCustomerName(),
                        selectedItem.getItemCode(), selectedItem.getItemName(),
                        selectedItem.getCategory(), selectedItem.getUnitPrice(),
                        qty, total, LocalDateTime.now(), false);
                state.getRestaurantOrderList().add(order);
                orderDAO.saveAll(state.getRestaurantOrderList());
                orderTable.refresh();
            } else {
                UIUtils.showAlert(Alert.AlertType.WARNING, "Selection Missing",
                        "Please select an occupied room and a menu item.");
            }
        });

        HBox actionBox = new HBox(15, new Label("Room:"), roomCombo, new Label("Qty:"), qtySpinner, btnOrder);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        actionBox.setPadding(new Insets(15));
        actionBox.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        Button btnImportMenu = UIUtils.createStyledButton("\u2B06 Import Menu", "#2980b9");
        btnImportMenu.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fc.showOpenDialog(null);
            if (file != null) {
                int added = 0, skipped = 0;
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line = br.readLine();
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(",", -1);
                        if (parts.length < 1 || parts[0].trim().isEmpty()) { skipped++; continue; }
                        String itemName = parts[0].trim();
                        if (state.getMenuItemList().stream()
                                .anyMatch(m -> m.getItemName().equals(itemName))) {
                            skipped++; continue;
                        }
                        String category = parts.length > 1 ? parts[1].trim() : "";
                        if (!"Starter".equals(category) && !"Main Course".equals(category)
                                && !"Dessert".equals(category) && !"Beverage".equals(category)) {
                            category = "Main Course";
                        }
                        double price = 0;
                        try {
                            if (parts.length > 2) price = Double.parseDouble(parts[2].trim());
                        } catch (NumberFormatException ex) { price = -1; }
                        if (price <= 0) { skipped++; continue; }
                        boolean available = true;
                        if (parts.length > 3) {
                            if ("false".equals(parts[3].trim().toLowerCase())) available = false;
                        }
                        if (!available) { skipped++; continue; }
                        String newCode = "M" + (state.getMenuItemList().size() + 101);
                        state.getMenuItemList().add(new MenuItem(newCode, itemName, category, price));
                        added++;
                    }
                    menuDAO.saveAll(state.getMenuItemList());
                    UIUtils.showAlert(Alert.AlertType.INFORMATION, "Import Complete",
                            "Menu Import Complete \u2014 " + added + " items added, " + skipped + " rows skipped.");
                } catch (IOException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Import Error",
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
        VBox root = new VBox(20,
                UIUtils.createSectionHeader("Dining & POS",
                        "Manage restaurant orders for checked-in guests"),
                split);
        root.setPadding(new Insets(30));
        return root;
    }
}

package com.mitgrandregency.hotel.ui;

import com.mitgrandregency.hotel.dao.RoomDAO;
import com.mitgrandregency.hotel.model.AppState;
import com.mitgrandregency.hotel.model.Room;

import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class HousekeepingView {

    private final AppState state;
    private final RoomDAO roomDAO;
    private final Runnable onDataChanged;
    private Runnable refreshPane;

    public HousekeepingView(AppState state, RoomDAO roomDAO, Runnable onDataChanged) {
        this.state = state;
        this.roomDAO = roomDAO;
        this.onDataChanged = onDataChanged;
    }

    public Runnable getRefreshCallback() { return refreshPane; }

    @SuppressWarnings("unchecked")
    public Node createView() {
        TableView<Room> table = new TableView<>();
        UIUtils.styleTable(table);
        FilteredList<Room> cleaningData = new FilteredList<>(state.getRoomList(),
                r -> "Cleaning".equals(r.getStatus()) || "Urgent Cleaning".equals(r.getStatus()));
        table.setItems(cleaningData);

        TableColumn<Room, String> cRoom = new TableColumn<>("Room #");
        cRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        cRoom.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TableColumn<Room, String> cType = new TableColumn<>("Type");
        cType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        TableColumn<Room, String> cStatus = new TableColumn<>("Status");
        cStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        cStatus.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                boolean urgent = "Urgent Cleaning".equals(item);
                Label badge = new Label(urgent ? "\uD83D\uDD34 URGENT" : "Cleaning");
                badge.setStyle(urgent
                    ? "-fx-background-color:#fdecea;-fx-text-fill:#c0392b;-fx-font-weight:bold;-fx-padding:3 10;-fx-background-radius:10;"
                    : "-fx-background-color:#fff3cd;-fx-text-fill:#856404;-fx-font-weight:bold;-fx-padding:3 10;-fx-background-radius:10;");
                setGraphic(badge);
            }
        });
        TableColumn<Room, Void> cAction = new TableColumn<>("Action");
        cAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnClean = new Button("Mark as Available");
            { btnClean.setStyle("-fx-background-color:#2ecc71;-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:5 10;-fx-background-radius:6;");
              btnClean.setOnAction(e -> { Room r = getTableView().getItems().get(getIndex()); r.setStatus("Available"); r.setPriority(false); onDataChanged.run(); }); }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : btnClean); }
        });
        table.getColumns().addAll(cRoom, cType, cStatus, cAction);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if ("Urgent Cleaning".equals(item.getStatus())) setStyle("-fx-background-color:#fff0f0;");
                else setStyle("");
            }
        });
        VBox root = new VBox(20, UIUtils.createSectionHeader("Housekeeping","Manage room cleaning states post-checkout"), table);
        root.setPadding(new Insets(30));
        VBox.setVgrow(table, Priority.ALWAYS);
        refreshPane = () -> { cleaningData.setPredicate(null); cleaningData.setPredicate(r -> "Cleaning".equals(r.getStatus()) || "Urgent Cleaning".equals(r.getStatus())); table.refresh(); };
        return root;
    }
}

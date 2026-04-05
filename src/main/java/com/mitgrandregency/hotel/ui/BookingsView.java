package com.mitgrandregency.hotel.ui;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.mitgrandregency.hotel.dao.OrderDAO;
import com.mitgrandregency.hotel.dao.RoomDAO;
import com.mitgrandregency.hotel.model.AppState;
import com.mitgrandregency.hotel.model.HistoryRecord;
import com.mitgrandregency.hotel.model.RestaurantOrder;
import com.mitgrandregency.hotel.model.Room;
import com.mitgrandregency.hotel.service.AadhaarStorageService;
import com.mitgrandregency.hotel.service.BookingService;
import com.mitgrandregency.hotel.service.EmailService;
import com.mitgrandregency.hotel.service.InvoiceService;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Bookings / room management panel with check-in, check-out, and extend logic.
 */
public class BookingsView {

    private final AppState state;
    private final BookingService bookingService;
    private final AadhaarStorageService aadhaarService;
    private final InvoiceService invoiceService;
    private final EmailService emailService;
    private final RoomDAO roomDAO;
    private final OrderDAO orderDAO;
    private final Runnable onDataChanged;

    private static final java.io.File INVOICE_DIR = new java.io.File("/home/tharun/Hotel Management/invoice");
    private byte[] selectedAadhaarBytes = null;

    public BookingsView(AppState state, BookingService bookingService,
                        AadhaarStorageService aadhaarService, InvoiceService invoiceService,
                        EmailService emailService, RoomDAO roomDAO, OrderDAO orderDAO,
                        Runnable onDataChanged) {
        this.state = state;
        this.bookingService = bookingService;
        this.aadhaarService = aadhaarService;
        this.invoiceService = invoiceService;
        this.emailService = emailService;
        this.roomDAO = roomDAO;
        this.orderDAO = orderDAO;
        this.onDataChanged = onDataChanged;
    }

    @SuppressWarnings("unchecked")
    public Node createView() {
        java.util.Map<Room, Boolean> recentlyBooked = new java.util.HashMap<>();

        TableView<Room> table = new TableView<>();
        table.setRowFactory(tv -> {
            TableRow<Room> row = new TableRow<>() {
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
            stHover.setToX(1.005); stHover.setToY(1.005);
            ScaleTransition stExit = new ScaleTransition(Duration.millis(100), row);
            stExit.setToX(1.0); stExit.setToY(1.0);
            row.setOnMouseEntered(e -> { if (!row.isEmpty()) stHover.playFromStart(); });
            row.setOnMouseExited(e -> { if (!row.isEmpty()) stExit.playFromStart(); });
            return row;
        });
        table.getStyleClass().add("table-view");
        table.setFixedCellSize(40);

        FilteredList<Room> filteredData = new FilteredList<>(state.getRoomList(), p -> true);
        table.setItems(filteredData);

        TableColumn<Room, String> colRoom = new TableColumn<>("Room #");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoom.setStyle("-fx-font-weight: bold;");

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<Room, String> colStat = new TableColumn<>("Status");
        colStat.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStat.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setPadding(new Insets(3, 12, 3, 12));
                if ("Available".equals(item)) {
                    badge.setStyle("-fx-background-color: #e8f8f0; -fx-text-fill: #27ae60; "
                            + "-fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 12;");
                } else if ("Cleaning".equals(item)) {
                    badge.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; "
                            + "-fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 12;");
                } else {
                    badge.setStyle("-fx-background-color: #fdecea; -fx-text-fill: #c0392b; "
                            + "-fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 12;");
                }
                setGraphic(badge); setText(null);
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
        colPriority.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null || !val) { setText(null); setGraphic(null); }
                else {
                    Label badge = new Label("URGENT");
                    badge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; "
                            + "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 6; "
                            + "-fx-background-radius: 8;");
                    setGraphic(badge); setText(null);
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
        filter.setOnAction(e -> filteredData.setPredicate(
                r -> "All Rooms".equals(filter.getValue()) || r.getStatus().equals(filter.getValue())));

        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + UIUtils.DARK_TEXT + ";");
        HBox filterBar = new HBox(10, filterLabel, filter);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Action Panel
        TextField txtCust = new TextField(); txtCust.setPromptText("Guest Name"); UIUtils.styleTextField(txtCust);
        TextField txtCont = new TextField(); txtCont.setPromptText("Contact Number"); UIUtils.styleTextField(txtCont);
        TextField txtEmail = new TextField(); txtEmail.setPromptText("Email Address"); UIUtils.styleTextField(txtEmail);
        TextField txtAddress = new TextField(); txtAddress.setPromptText("Home Address"); UIUtils.styleTextField(txtAddress);
        Spinner<Integer> spinDays = new Spinner<>(1, 30, 1);
        spinDays.setPrefWidth(100);
        spinDays.setStyle("-fx-font-size: 13px;");

        Button btnBook = new Button("\u2714 Book Room");
        btnBook.setStyle("-fx-background-color: #c9a96e; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-padding: 8 15; -fx-font-size: 13px; -fx-background-radius: 6;");
        btnBook.setMaxWidth(Double.MAX_VALUE);

        Button btnOut = new Button("\u2717 Checkout");
        btnOut.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-padding: 8 15; -fx-font-size: 13px; -fx-background-radius: 6;");
        btnOut.setMaxWidth(Double.MAX_VALUE);

        Button btnExt = new Button("\u23F1 Extend Stay");
        btnExt.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-padding: 8 15; -fx-font-size: 13px; -fx-background-radius: 6;");
        btnExt.setMaxWidth(Double.MAX_VALUE);

        Button btnUrgent = new Button("\u26A0 Mark Urgent");
        btnUrgent.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-padding: 8 15; -fx-font-size: 13px; -fx-background-radius: 6;");
        btnUrgent.setMaxWidth(Double.MAX_VALUE);

        ImageView aadhaarPreview = new ImageView();
        aadhaarPreview.setFitWidth(150);
        aadhaarPreview.setFitHeight(100);
        aadhaarPreview.setPreserveRatio(true);
        VBox previewBox = new VBox(aadhaarPreview);
        previewBox.setAlignment(Pos.CENTER);
        previewBox.setPrefSize(150, 100);
        previewBox.setStyle("-fx-border-color: #aaa; -fx-border-style: dashed; -fx-background-color: #f9f9f9;");

        Button btnUpload = new Button("\uD83D\uDCC1 Upload Image");
        btnUpload.setStyle("-fx-font-size: 10px;");
        Button btnWebcam = new Button("\uD83D\uDCF7 Capture");
        btnWebcam.setStyle("-fx-font-size: 10px;");
        btnWebcam.setDisable(true);
        btnWebcam.setTooltip(new Tooltip("Webcam capture requires external hardware"));

        btnUpload.setOnAction(evt -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fc.showOpenDialog(btnUpload.getScene().getWindow());
            if (file != null) {
                if (file.length() > 5 * 1024 * 1024) {
                    UIUtils.showAlert(Alert.AlertType.WARNING, "File Too Large",
                            "Aadhaar image must be under 5MB.");
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

        String labelStyle = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + UIUtils.DARK_TEXT + ";";
        GridPane grid = new GridPane();
        grid.setVgap(12); grid.setHgap(12); grid.setPadding(new Insets(15));
        grid.add(new Label("Guest Name") {{ setStyle(labelStyle); }}, 0, 0);
        grid.add(txtCust, 0, 1);
        grid.add(new Label("Phone") {{ setStyle(labelStyle); }}, 0, 2);
        grid.add(txtCont, 0, 3);
        grid.add(new Label("Email Address") {{ setStyle(labelStyle); }}, 0, 4);
        grid.add(txtEmail, 0, 5);
        grid.add(new Label("Home Address") {{ setStyle(labelStyle); }}, 0, 6);
        grid.add(txtAddress, 0, 7);
        grid.add(new Label("Aadhaar Card Image:") {{ setStyle(labelStyle); }}, 0, 8);
        grid.add(previewBox, 0, 9);
        grid.add(uploadRow, 0, 10);
        grid.add(new Label("Duration (Days)") {{ setStyle(labelStyle); }}, 0, 11);
        grid.add(spinDays, 0, 12);

        VBox btnRow = new VBox(10, btnBook, btnExt, btnOut, btnUrgent);
        btnRow.setPadding(new Insets(8, 0, 0, 0));
        grid.add(btnRow, 0, 13);

        Label actionTitle = new Label("Action Desk");
        actionTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        actionTitle.setStyle("-fx-text-fill: " + UIUtils.DARK_TEXT + ";");
        Region actionSep = new Region();
        actionSep.setPrefHeight(2); actionSep.setMaxWidth(Double.MAX_VALUE);
        actionSep.setStyle("-fx-background-color: " + UIUtils.GOLD_ACCENT + "; -fx-opacity: 0.5;");

        ScrollPane actionScroll = new ScrollPane(grid);
        actionScroll.setFitToWidth(true);
        actionScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        actionScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        actionScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; "
                + "-fx-border-color: transparent;");
        actionScroll.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(actionScroll, Priority.ALWAYS);

        VBox actionCard = new VBox(10, actionTitle, actionSep, actionScroll);
        actionCard.setPadding(new Insets(20));
        actionCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.0, 0, 3);");
        actionCard.setPrefWidth(320);
        actionCard.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(actionCard, Priority.ALWAYS);

        // Context-aware buttons
        Consumer<Room> updateActionForm = sel -> {
            btnBook.setVisible(false); btnBook.setManaged(false);
            btnExt.setVisible(false); btnExt.setManaged(false);
            btnOut.setVisible(false); btnOut.setManaged(false);
            btnUrgent.setVisible(false); btnUrgent.setManaged(false);
            if (sel == null) {
                txtCust.clear(); txtCont.clear(); txtEmail.clear(); txtAddress.clear();
                aadhaarPreview.setImage(null); selectedAadhaarBytes = null;
                return;
            }
            if ("Available".equals(sel.getStatus())) {
                btnBook.setVisible(true); btnBook.setManaged(true);
                txtCust.clear(); txtCont.clear(); txtEmail.clear(); txtAddress.clear();
                aadhaarPreview.setImage(null); selectedAadhaarBytes = null;
                txtCust.setEditable(true); txtCont.setEditable(true);
                txtEmail.setEditable(true); txtAddress.setEditable(true);
                spinDays.setDisable(false); btnUpload.setDisable(false);
            } else if ("Occupied".equals(sel.getStatus())) {
                btnExt.setVisible(true); btnExt.setManaged(true);
                btnOut.setVisible(true); btnOut.setManaged(true);
                txtCust.setText(sel.getCustomerName() != null ? sel.getCustomerName() : "");
                txtCont.setText(sel.getContactNumber() != null ? sel.getContactNumber() : "");
                txtEmail.setText(sel.getGuestEmail() != null ? sel.getGuestEmail() : "");
                txtAddress.setText(sel.getGuestAddress() != null ? sel.getGuestAddress() : "");
                Image img = aadhaarService.loadAadhaarImage(sel.getAadhaarPath());
                aadhaarPreview.setImage(img);
                selectedAadhaarBytes = null;
                txtCust.setEditable(false); txtCont.setEditable(false);
                txtEmail.setEditable(false); txtAddress.setEditable(false);
                spinDays.setDisable(true); btnUpload.setDisable(true);
            } else if ("Cleaning".equals(sel.getStatus()) || "Urgent Cleaning".equals(sel.getStatus())) {
                btnUrgent.setVisible(true); btnUrgent.setManaged(true);
                if ("Urgent Cleaning".equals(sel.getStatus())) {
                    btnUrgent.setText("\u26A0 Already Urgent"); btnUrgent.setDisable(true);
                } else {
                    btnUrgent.setText("\u26A0 Mark Urgent"); btnUrgent.setDisable(false);
                }
                txtCust.clear(); txtCont.clear(); txtEmail.clear(); txtAddress.clear();
                aadhaarPreview.setImage(null); selectedAadhaarBytes = null;
            } else {
                txtCust.clear(); txtCont.clear(); txtEmail.clear(); txtAddress.clear();
                aadhaarPreview.setImage(null); selectedAadhaarBytes = null;
            }
        };

        btnBook.setVisible(false); btnBook.setManaged(false);
        btnExt.setVisible(false); btnExt.setManaged(false);
        btnOut.setVisible(false); btnOut.setManaged(false);
        btnUrgent.setVisible(false); btnUrgent.setManaged(false);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> updateActionForm.accept(sel));

        // BOOK
        btnBook.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r == null || txtCust.getText().isEmpty()) return;
            if (selectedAadhaarBytes == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "Missing Image",
                        "Aadhaar card image is required for check-in.");
                return;
            }
            try {
                String path = aadhaarService.saveAadhaarImage(
                        selectedAadhaarBytes, r.getRoomNumber(), txtCust.getText());
                bookingService.checkIn(r, txtCust.getText(), txtCont.getText(),
                        txtEmail.getText(), txtAddress.getText(), spinDays.getValue(), path);
            } catch (IOException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "File Error",
                        "Could not save Aadhaar image: " + ex.getMessage());
                return;
            }
            selectedAadhaarBytes = null;
            aadhaarPreview.setImage(null);
            recentlyBooked.put(r, true);
            table.refresh();
            onDataChanged.run();
        });

        // EXTEND
        btnExt.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r == null) return;
            TextInputDialog d = new TextInputDialog("1");
            d.setTitle("Extend Stay");
            d.setHeaderText("Additional Days:");
            d.setContentText("Enter number of days:");
            d.showAndWait().ifPresent(days -> {
                bookingService.extendStay(r, Integer.parseInt(days));
                table.refresh();
                onDataChanged.run();
            });
        });

        // URGENT
        btnUrgent.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r == null) return;
            r.setStatus("Urgent Cleaning");
            table.refresh();
            onDataChanged.run();
            btnUrgent.setText("\u26A0 Already Urgent");
            btnUrgent.setDisable(true);
        });

        // CHECKOUT
        btnOut.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r == null) return;

            BookingService.CheckoutResult result = bookingService.prepareCheckout(r);
            HistoryRecord record = result.record();
            double resTotal = result.restaurantTotal();

            // Preview dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Checkout Confirmation");
            dialog.setHeaderText("Checkout preview for Room " + r.getRoomNumber());
            GridPane previewGrid = new GridPane();
            previewGrid.setHgap(10); previewGrid.setVgap(10);
            previewGrid.setPadding(new Insets(20, 150, 10, 10));
            previewGrid.add(new Label("Guest:"), 0, 0);
            previewGrid.add(new Label(record.getGuestName()), 1, 0);
            previewGrid.add(new Label("Email:"), 0, 1);
            previewGrid.add(new Label(record.getGuestEmail() != null && !record.getGuestEmail().isEmpty()
                    ? record.getGuestEmail() : "\u2014"), 1, 1);
            previewGrid.add(new Label("Check-In:"), 0, 2);
            previewGrid.add(new Label(record.getCheckInDate()), 1, 2);
            previewGrid.add(new Label("Nights:"), 0, 3);
            previewGrid.add(new Label(String.valueOf(record.getNights())), 1, 3);
            previewGrid.add(new Label("Room Subtotal:"), 0, 4);
            previewGrid.add(new Label(String.format("\u20B9 %.2f", record.getSubtotal())), 1, 4);
            previewGrid.add(new Label("GST (" + (int) state.getGstRate() + "%):"), 0, 5);
            previewGrid.add(new Label(String.format("\u20B9 %.2f", record.getTaxAmount())), 1, 5);
            previewGrid.add(new Label("Dining/POS:"), 0, 6);
            previewGrid.add(new Label(String.format("\u20B9 %.2f", resTotal)), 1, 6);
            Label totalLabel = new Label(String.format("\u20B9 %.2f", record.getTotalPaid()));
            totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            previewGrid.add(new Label("Grand Total:"), 0, 7);
            previewGrid.add(totalLabel, 1, 7);

            dialog.getDialogPane().setContent(previewGrid);
            ButtonType btnConfirmPdf = new ButtonType("Confirm & Generate Invoice", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnConfirmPdf, ButtonType.CANCEL);

            Optional<ButtonType> dialogResult = dialog.showAndWait();
            if (dialogResult.isEmpty() || dialogResult.get() != btnConfirmPdf) return;

            // Payment Mode Dialog
            Dialog<ButtonType> paymentDialog = new Dialog<>();
            paymentDialog.setTitle("Payment Details");
            paymentDialog.setHeaderText("Select Payment Mode");
            
            ToggleGroup tg = new ToggleGroup();
            RadioButton rbCash = new RadioButton("Cash");
            RadioButton rbCard = new RadioButton("Card");
            RadioButton rbUpi = new RadioButton("UPI");
            RadioButton rbBank = new RadioButton("Bank Transfer");
            rbCash.setToggleGroup(tg);
            rbCard.setToggleGroup(tg);
            rbUpi.setToggleGroup(tg);
            rbBank.setToggleGroup(tg);
            
            TextField txtRef = new TextField();
            txtRef.setPromptText("Transaction / Reference ID");
            txtRef.setDisable(true);
            
            tg.selectedToggleProperty().addListener((obs, oldV, newV) -> {
                if (newV == rbCash) {
                    txtRef.setDisable(true);
                    txtRef.clear();
                } else if (newV != null) {
                    txtRef.setDisable(false);
                }
            });
            
            VBox pVbox = new VBox(10, rbCash, rbCard, rbUpi, rbBank, new Label("Transaction ID:"), txtRef);
            pVbox.setPadding(new Insets(20));
            paymentDialog.getDialogPane().setContent(pVbox);
            
            ButtonType btnConfirmPayment = new ButtonType("Confirm Payment", ButtonBar.ButtonData.OK_DONE);
            paymentDialog.getDialogPane().getButtonTypes().addAll(btnConfirmPayment, ButtonType.CANCEL);
            
            Button btnPaymentButton = (Button) paymentDialog.getDialogPane().lookupButton(btnConfirmPayment);
            btnPaymentButton.setStyle("-fx-background-color: #c9a96e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6;");

            btnPaymentButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                RadioButton selected = (RadioButton) tg.getSelectedToggle();
                if (selected == null) {
                    UIUtils.showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a payment mode.");
                    event.consume();
                    return;
                }
                if (selected != rbCash && txtRef.getText().trim().isEmpty()) {
                    UIUtils.showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a Transaction / Reference ID.");
                    event.consume();
                    return;
                }
            });
            
            Optional<ButtonType> payRes = paymentDialog.showAndWait();
            if (payRes.isEmpty() || payRes.get() != btnConfirmPayment) return;
            
            RadioButton selectedRb = (RadioButton) tg.getSelectedToggle();
            record.setPaymentMode(selectedRb.getText());
            record.setTransactionId(txtRef.getText().trim());

            // File chooser
            if (!INVOICE_DIR.exists()) INVOICE_DIR.mkdirs();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Invoice PDF");
            fileChooser.setInitialDirectory(INVOICE_DIR);
            fileChooser.setInitialFileName(String.format("Invoice_Room%s_%s.pdf",
                    r.getRoomNumber(), r.getCustomerName().replaceAll("[^a-zA-Z0-9]", "")));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(btnOut.getScene().getWindow());
            if (file == null) {
                UIUtils.showAlert(Alert.AlertType.INFORMATION, "Cancelled",
                        "Checkout cancelled \u2014 no file selected.");
                return;
            }

            // Generate PDF & commit
            try {
                bookingService.commitCheckout(r, result);
                invoiceService.generateInvoicePDF(file, record, result.settledOrders(), resTotal);
                orderDAO.saveAll(state.getRestaurantOrderList());
                table.refresh();
                onDataChanged.run();
            } catch (Throwable ex) {
                bookingService.rollbackCheckout(result);
                UIUtils.showAlert(Alert.AlertType.ERROR, "Checkout Failed",
                        "Could not complete checkout or generate PDF:\n" + ex.getMessage());
                ex.printStackTrace();
                return;
            }

            // Post-checkout dialog
            boolean hasEmail = record.getGuestEmail() != null && !record.getGuestEmail().isEmpty();
            ButtonType btnSendEmail = new ButtonType("\uD83D\uDCE7 Send Invoice via Email", ButtonBar.ButtonData.LEFT);
            ButtonType btnSaveAnother = new ButtonType("\uD83D\uDDA8\uFE0F Save Another Copy", ButtonBar.ButtonData.OTHER);
            ButtonType btnDone = new ButtonType("\u2714 Done", ButtonBar.ButtonData.OK_DONE);

            Alert postDialog = new Alert(Alert.AlertType.INFORMATION);
            postDialog.setTitle("Checkout Complete");
            postDialog.setHeaderText("\u2705 Invoice saved successfully");
            postDialog.setContentText("PDF saved to:\n" + file.getAbsolutePath()
                    + (hasEmail ? "\n\nRegistered email: " + record.getGuestEmail()
                    : "\n\n\u26A0 No email on file for this guest."));
            if (hasEmail) {
                postDialog.getButtonTypes().setAll(btnSendEmail, btnSaveAnother, btnDone);
            } else {
                postDialog.getButtonTypes().setAll(btnSaveAnother, btnDone);
            }

            Optional<ButtonType> postResult = postDialog.showAndWait();
            if (postResult.isPresent() && postResult.get() == btnSendEmail) {
                String guestEmail = record.getGuestEmail();
                File invoiceFile = file;
                new Thread(() -> {
                    try {
                        emailService.sendInvoice(guestEmail, record.getGuestName(), record.getRoomNumber(), invoiceFile);
                        javafx.application.Platform.runLater(() ->
                                UIUtils.showAlert(Alert.AlertType.INFORMATION, "Email Sent",
                                        "Invoice successfully emailed to:\n" + guestEmail));
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() ->
                                UIUtils.showAlert(Alert.AlertType.WARNING, "Email Failed",
                                        "Invoice was saved but the email could not be sent.\n\nReason: " + ex.getMessage()));
                    }
                }).start();
            } else if (postResult.isPresent() && postResult.get() == btnSaveAnother) {
                FileChooser fc2 = new FileChooser();
                fc2.setTitle("Save Another Copy");
                fc2.setInitialDirectory(INVOICE_DIR);
                fc2.setInitialFileName("Copy_" + file.getName());
                fc2.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                File file2 = fc2.showSaveDialog(null);
                if (file2 != null) {
                    try {
                        invoiceService.generateInvoicePDF(file2, record, result.settledOrders(), resTotal);
                        UIUtils.showAlert(Alert.AlertType.INFORMATION, "Saved",
                                "Copy saved to:\n" + file2.getAbsolutePath());
                    } catch (Exception ex2) {
                        UIUtils.showAlert(Alert.AlertType.ERROR, "Save Failed", ex2.getMessage());
                    }
                }
            }
        });

        // Layout
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
                UIUtils.createSectionHeader("Room Bookings",
                        "Manage guest check-in, check-out, and stay extensions"),
                mainContent);
        root.setPadding(new Insets(30));
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        return root;
    }
}

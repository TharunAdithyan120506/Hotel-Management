package com.mitgrandregency.hotel.ui;

import com.mitgrandregency.hotel.model.AppState;
import com.mitgrandregency.hotel.model.HistoryRecord;
import com.mitgrandregency.hotel.service.EmailService;
import com.mitgrandregency.hotel.service.InvoiceService;
import com.mitgrandregency.hotel.service.ReportService;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LedgerView {

    private final AppState state;
    private final InvoiceService invoiceService;
    private final EmailService emailService;
    private final ReportService reportService;
    private static final java.io.File INVOICE_DIR = new java.io.File("/home/tharun/Hotel Management/invoice");
    private Label ledgerSummaryLabel;

    public LedgerView(AppState state, InvoiceService invoiceService,
                      EmailService emailService, ReportService reportService) {
        this.state = state;
        this.invoiceService = invoiceService;
        this.emailService = emailService;
        this.reportService = reportService;
    }

    public void updateSummary() {
        if (ledgerSummaryLabel != null)
            ledgerSummaryLabel.setText(reportService.getLedgerSummary());
    }

    @SuppressWarnings("unchecked")
    public Node createView() {
        TableView<HistoryRecord> table = new TableView<>();
        UIUtils.styleTable(table);
        table.setItems(state.getHistoryList());

        TableColumn<HistoryRecord, String> c1 = new TableColumn<>("Room #");
        c1.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        c1.setStyle("-fx-font-weight: bold;");
        TableColumn<HistoryRecord, String> c2 = new TableColumn<>("Type");
        c2.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        TableColumn<HistoryRecord, String> c3 = new TableColumn<>("Guest Name");
        c3.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        TableColumn<HistoryRecord, String> cCon = new TableColumn<>("Contact");
        cCon.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        TableColumn<HistoryRecord, String> c4 = new TableColumn<>("Check-In");
        c4.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        TableColumn<HistoryRecord, String> c5 = new TableColumn<>("Check-Out");
        c5.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        TableColumn<HistoryRecord, Long> c6 = new TableColumn<>("Nights");
        c6.setCellValueFactory(new PropertyValueFactory<>("nights"));
        TableColumn<HistoryRecord, Double> c7 = new TableColumn<>("Amount Paid (Rs)");
        c7.setCellValueFactory(new PropertyValueFactory<>("totalPaid"));
        c7.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean e) {
                super.updateItem(v, e);
                setText(e || v == null ? null : String.format("Rs. %.2f", v));
            }
        });
        TableColumn<HistoryRecord, LocalDateTime> c8 = new TableColumn<>("Booked At");
        c8.setCellValueFactory(new PropertyValueFactory<>("bookedAt"));
        c8.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime v, boolean e) {
                super.updateItem(v, e);
                setText(e || v == null ? null : v.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
            }
        });

        TableColumn<HistoryRecord, Void> c10 = new TableColumn<>("Invoice");
        c10.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("\u2B07 PDF");
            { btn.setStyle("-fx-background-color:#2a1a3c;-fx-text-fill:white;-fx-font-size:11px;-fx-cursor:hand;");
              btn.setOnAction(ev -> {
                HistoryRecord data = getTableView().getItems().get(getIndex());
                if (!INVOICE_DIR.exists()) INVOICE_DIR.mkdirs();
                FileChooser fc = new FileChooser();
                fc.setInitialDirectory(INVOICE_DIR);
                fc.setInitialFileName("Reinvoice_Room" + data.getRoomNumber() + "_" + data.getGuestName() + ".pdf");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"));
                File f = fc.showSaveDialog(null);
                if (f == null) return;
                try { invoiceService.generateInvoicePDF(f, data, null, 0.0); }
                catch (Exception ex) { UIUtils.showAlert(Alert.AlertType.ERROR, "PDF Error", ex.getMessage()); return; }
                boolean hasEmail = data.getGuestEmail() != null && !data.getGuestEmail().isEmpty();
                ButtonType btnMail = new ButtonType("\uD83D\uDCE7 Send via Email", ButtonBar.ButtonData.LEFT);
                ButtonType btnCopy = new ButtonType("\uD83D\uDDA8\uFE0F Save Another Copy", ButtonBar.ButtonData.OTHER);
                ButtonType btnClose = new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);
                Alert post = new Alert(Alert.AlertType.INFORMATION);
                post.setTitle("Invoice Saved");
                post.setHeaderText("Re-invoice saved for " + data.getGuestName());
                post.setContentText("File: " + f.getAbsolutePath() + (hasEmail ? "\nEmail: " + data.getGuestEmail() : "\n\u26A0 No email on file."));
                if (hasEmail) post.getButtonTypes().setAll(btnMail, btnCopy, btnClose);
                else post.getButtonTypes().setAll(btnCopy, btnClose);
                Optional<ButtonType> res = post.showAndWait();
                if (res.isPresent() && res.get() == btnMail && hasEmail) {
                    String email = data.getGuestEmail();
                    new Thread(() -> {
                        try { emailService.sendInvoice(email, data.getGuestName(), data.getRoomNumber(), f);
                              javafx.application.Platform.runLater(() -> UIUtils.showAlert(Alert.AlertType.INFORMATION, "Email Sent", "Invoice emailed to: " + email)); }
                        catch (Exception ex) { javafx.application.Platform.runLater(() -> UIUtils.showAlert(Alert.AlertType.WARNING, "Email Failed", ex.getMessage())); }
                    }).start();
                } else if (res.isPresent() && res.get() == btnCopy) {
                    FileChooser fc2 = new FileChooser();
                    fc2.setInitialDirectory(INVOICE_DIR);
                    fc2.setInitialFileName("Copy_" + f.getName());
                    fc2.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                    File f2 = fc2.showSaveDialog(null);
                    if (f2 != null) try { invoiceService.generateInvoicePDF(f2, data, null, 0.0); } catch (Exception ex2) { UIUtils.showAlert(Alert.AlertType.ERROR, "Error", ex2.getMessage()); }
                }
              }); }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : btn); }
        });

        table.getColumns().addAll(c1, c2, c3, cCon, c4, c5, c6, c7, c8, c10);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ledgerSummaryLabel = new Label();
        ledgerSummaryLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.DARK_TEXT + ";");
        ledgerSummaryLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(ledgerSummaryLabel, Priority.ALWAYS);
        state.getHistoryList().addListener((ListChangeListener<HistoryRecord>) c -> updateSummary());

        Button btnExport = UIUtils.createStyledButton("\u2B07 Export Full Ledger as CSV", UIUtils.ROYAL_BLUE);
        btnExport.setOnAction(ev -> {
            if (!INVOICE_DIR.exists()) INVOICE_DIR.mkdirs();
            FileChooser fc = new FileChooser();
            fc.setTitle("Save Ledger CSV");
            fc.setInitialDirectory(INVOICE_DIR);
            fc.setInitialFileName("MIT_Grand_Regency_Ledger_" + LocalDate.now() + ".csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fc.showSaveDialog(null);
            if (file != null) {
                try (PrintWriter w = new PrintWriter(file)) {
                    w.println("Room#,Type,Guest Name,Contact,Check-In,Check-Out,Nights,Rate,Subtotal,GST%,Tax,Grand Total,Booked At");
                    for (HistoryRecord hr : state.getHistoryList()) w.println(hr.toCSV());
                    UIUtils.showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Ledger exported to:\n" + file.getAbsolutePath());
                } catch (Exception ex) { UIUtils.showAlert(Alert.AlertType.ERROR, "Export Failed", ex.getMessage()); }
            }
        });

        HBox summaryBar = new HBox(15, ledgerSummaryLabel, btnExport);
        summaryBar.setAlignment(Pos.CENTER_LEFT);
        summaryBar.setPadding(new Insets(14, 20, 14, 20));
        summaryBar.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0.0,0,2);");

        VBox root = new VBox(20, UIUtils.createSectionHeader("Financial Ledger","Complete checkout history and revenue records"), summaryBar, table);
        root.setPadding(new Insets(30));
        VBox.setVgrow(table, Priority.ALWAYS);
        updateSummary();
        return root;
    }
}

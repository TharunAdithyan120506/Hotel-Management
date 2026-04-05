package com.mitgrandregency.hotel.service;

import com.mitgrandregency.hotel.model.HistoryRecord;
import com.mitgrandregency.hotel.model.RestaurantOrder;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Generates branded PDF invoices using Apache PDFBox.
 */
public class InvoiceService {

    /**
     * Generates a professional PDF invoice for the given checkout record.
     *
     * @param file     destination file
     * @param hr       the checkout history record
     * @param orders   restaurant orders to include (may be null)
     * @param resTotal total restaurant charges
     * @throws IOException if the PDF cannot be written
     */
    public void generateInvoicePDF(File file, HistoryRecord hr,
                                   List<RestaurantOrder> orders, double resTotal)
            throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            String invoiceNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float[] GOLD  = { 201 / 255f, 169 / 255f, 110 / 255f };
            float[] WHITE = { 1f, 1f, 1f };
            float[] BLACK = { 0f, 0f, 0f };
            float[] GRAY  = { 100 / 255f, 100 / 255f, 100 / 255f };

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                // Header gold band
                cs.setNonStrokingColor(GOLD[0], GOLD[1], GOLD[2]);
                cs.addRect(0, 740, 612, 52);
                cs.fill();

                // Hotel name
                cs.beginText();
                cs.setFont(fontBold, 24);
                cs.setNonStrokingColor(WHITE[0], WHITE[1], WHITE[2]);
                cs.newLineAtOffset(50, 755);
                cs.showText("MIT Grand Regency");
                cs.endText();

                // Gold rule
                cs.setStrokingColor(GOLD[0], GOLD[1], GOLD[2]);
                cs.setLineWidth(2f);
                cs.moveTo(50, 720);
                cs.lineTo(562, 720);
                cs.stroke();

                // Invoice metadata (left)
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontBold, 16);
                cs.newLineAtOffset(50, 690);
                cs.showText("INVOICE");
                cs.setFont(fontNormal, 12);
                cs.newLineAtOffset(0, -20);
                cs.showText("Invoice Number: " + invoiceNumber);
                cs.newLineAtOffset(0, -15);
                cs.showText("Date: " + LocalDate.now().toString());
                cs.newLineAtOffset(0, -15);
                cs.showText("Room: " + hr.getRoomNumber() + " (" + hr.getRoomType() + ")");
                cs.endText();

                // Billed To (right)
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(350, 690);
                cs.showText("Billed To:");
                cs.setFont(fontNormal, 12);
                cs.newLineAtOffset(0, -15);
                cs.showText(hr.getGuestName());
                cs.newLineAtOffset(0, -15);
                cs.showText("Contact: " + hr.getContactNumber());
                cs.newLineAtOffset(0, -15);
                cs.showText("Check-In:  " + hr.getCheckInDate());
                cs.newLineAtOffset(0, -15);
                cs.showText("Total Nights: " + hr.getNights());
                cs.endText();

                // Table header
                int tableTop = 560;
                cs.setStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setLineWidth(1f);
                cs.moveTo(50, tableTop);
                cs.lineTo(562, tableTop);
                cs.moveTo(50, tableTop - 25);
                cs.lineTo(562, tableTop - 25);
                cs.stroke();

                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(60, tableTop - 16);
                cs.showText("Description");
                cs.newLineAtOffset(240, 0);
                cs.showText("Quantity");
                cs.newLineAtOffset(80, 0);
                cs.showText("Unit Rate");
                cs.newLineAtOffset(100, 0);
                cs.showText("Amount");
                cs.endText();

                // Room charge row
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontNormal, 12);
                cs.newLineAtOffset(60, tableTop - 45);
                cs.showText(hr.getRoomType() + " Room (" + hr.getNights() + " nights)");
                cs.newLineAtOffset(240, 0);
                cs.showText("1");
                cs.newLineAtOffset(80, 0);
                cs.showText(String.format("Rs. %.2f", hr.getSubtotal()));
                cs.newLineAtOffset(100, 0);
                cs.showText(String.format("Rs. %.2f", hr.getSubtotal()));
                cs.endText();

                int currentY = tableTop - 65;

                // Restaurant line items
                if (orders != null && !orders.isEmpty()) {
                    for (RestaurantOrder o : orders) {
                        cs.beginText();
                        cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                        cs.setFont(fontNormal, 12);
                        cs.newLineAtOffset(60, currentY);
                        cs.showText("Dining: " + o.getItemName());
                        cs.newLineAtOffset(240, 0);
                        cs.showText(String.valueOf(o.getQuantity()));
                        cs.newLineAtOffset(80, 0);
                        cs.showText(String.format("Rs. %.2f", o.getUnitPrice()));
                        cs.newLineAtOffset(100, 0);
                        cs.showText(String.format("Rs. %.2f", o.getTotalPrice()));
                        cs.endText();
                        currentY -= 20;
                    }
                }

                // GST row
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontNormal, 12);
                cs.newLineAtOffset(60, currentY);
                cs.showText(String.format("GST (%.1f%%)", hr.getGstRate()));
                cs.newLineAtOffset(240, 0);
                cs.showText("-");
                cs.newLineAtOffset(80, 0);
                cs.showText("-");
                cs.newLineAtOffset(100, 0);
                cs.showText(String.format("Rs. %.2f", hr.getTaxAmount()));
                cs.endText();

                // Bottom border
                cs.setStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.moveTo(50, currentY - 20);
                cs.lineTo(562, currentY - 20);
                cs.stroke();

                // Grand total
                cs.beginText();
                cs.setNonStrokingColor(BLACK[0], BLACK[1], BLACK[2]);
                cs.setFont(fontBold, 14);
                cs.newLineAtOffset(350, currentY - 45);
                cs.showText("Grand Total:");
                cs.newLineAtOffset(100, 0);
                cs.showText(String.format("Rs. %.2f", hr.getTotalPaid()));
                cs.endText();

                // Footer
                cs.beginText();
                cs.setNonStrokingColor(GRAY[0], GRAY[1], GRAY[2]);
                cs.setFont(fontNormal, 10);
                cs.newLineAtOffset(50, 100);
                cs.showText("Thank you for staying at MIT Grand Regency. We hope to see you again soon.");
                cs.newLineAtOffset(0, -15);
                cs.showText("This is a computer-generated invoice and does not require a physical signature.");
                cs.endText();
            }

            document.save(file);
        }
    }
}

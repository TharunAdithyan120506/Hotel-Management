package com.mitgrandregency.hotel.service;

import com.mitgrandregency.hotel.dao.ConfigLoader;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.util.Properties;

/**
 * Sends invoice emails via Gmail SMTP using Jakarta Mail.
 * Credentials are loaded from {@link ConfigLoader}.
 */
public class EmailService {

    private final String fromEmail;
    private final String appPassword;

    public EmailService(ConfigLoader config) {
        this.fromEmail = config.getMailUsername();
        this.appPassword = config.getMailPassword();
    }

    /**
     * Sends a branded HTML email with the PDF invoice attached.
     *
     * @param toEmail    recipient email address
     * @param guestName  guest name for the email body
     * @param roomNumber room number for the subject line
     * @param pdfFile    generated invoice PDF to attach
     * @throws Exception if SMTP connection or send fails
     */
    public void sendInvoice(String toEmail, String guestName, String roomNumber, File pdfFile)
            throws Exception {
        System.out.println("[EmailService] Attempting to connect to SMTP server for: " + toEmail);

        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        prop.put("mail.smtp.connectiontimeout", "15000");
        prop.put("mail.smtp.timeout", "15000");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            System.out.println("[EmailService] Authenticating and assembling MIME message...");
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your Invoice \u2014 MIT Grand Regency, Room " + roomNumber);

            String htmlBody = "<div style=\"font-family:Georgia,serif;color:#1a1a2e;max-width:600px;\">\n"
                    + "  <div style=\"background:#c9a96e;padding:20px;\">\n"
                    + "    <h1 style=\"color:white;margin:0;\">MIT Grand Regency</h1>\n"
                    + "    <p style=\"color:white;margin:0;font-size:12px;\">Luxury Hospitality Management</p>\n"
                    + "  </div>\n"
                    + "  <div style=\"padding:20px;\">\n"
                    + "    <p>Dear <strong>" + guestName + "</strong>,</p>\n"
                    + "    <p>Thank you for staying with us. Please find your invoice attached for Room <strong>"
                    + roomNumber + "</strong>.</p>\n"
                    + "    <p>We look forward to welcoming you again.</p>\n"
                    + "    <p style=\"color:#c9a96e;font-weight:bold;\">\u2014 MIT Grand Regency Team</p>\n"
                    + "  </div>\n"
                    + "</div>";

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(htmlPart);

            if (pdfFile != null && pdfFile.exists()) {
                System.out.println("[EmailService] Attaching PDF Invoice. Size: " + pdfFile.length() + " bytes");
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(pdfFile);
                attachPart.setFileName("Invoice_Room" + roomNumber + ".pdf");
                mp.addBodyPart(attachPart);
            }

            message.setContent(mp);

            System.out.println("[EmailService] Transport.send() triggered...");
            Transport.send(message);
            System.out.println("[EmailService] Email sent successfully to: " + toEmail);

        } catch (Exception ex) {
            System.err.println("[EmailService] FAILED: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("SMTP send failed: " + ex.getMessage(), ex);
        }
    }
}

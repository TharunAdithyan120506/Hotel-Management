package com.mitgrandregency.hotel.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads application configuration from the {@code hotel_settings.properties} file.
 * This class handles properties-file I/O only — database settings are managed
 * by {@link SettingsDAO}.
 */
public class ConfigLoader {

    private static final String PROPS_FILE = "hotel_settings.properties";

    private String dbUrl = "jdbc:mariadb://localhost:3306/mit_grand_regency";
    private String dbUser = "";
    private String dbPassword = "";
    private String mailUsername = "";
    private String mailPassword = "";
    private double priceSingle = 1000.0;
    private double priceDouble = 2000.0;
    private double priceDeluxe = 3500.0;
    private double gstRate = 18.0;

    public ConfigLoader() {
        loadConfig();
    }

    private void loadConfig() {
        File file = new File(PROPS_FILE);
        if (!file.exists()) {
            System.err.println("[ConfigLoader] WARNING: " + PROPS_FILE + " not found. "
                    + "Copy hotel_settings.properties.example and fill in your values.");
            return;
        }
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);

            dbUrl = props.getProperty("db.url", dbUrl);
            dbUser = props.getProperty("db.user", dbUser);
            dbPassword = props.getProperty("db.password", dbPassword);
            mailUsername = props.getProperty("mail.username", mailUsername);
            mailPassword = props.getProperty("mail.password", mailPassword);

            String val;
            val = props.getProperty("price.single");
            if (val != null) priceSingle = Double.parseDouble(val);
            val = props.getProperty("price.double");
            if (val != null) priceDouble = Double.parseDouble(val);
            val = props.getProperty("price.deluxe");
            if (val != null) priceDeluxe = Double.parseDouble(val);
            val = props.getProperty("gst.rate");
            if (val != null) gstRate = Double.parseDouble(val);

            // Legacy key support (old properties files used different key names)
            val = props.getProperty("priceSingle");
            if (val != null) priceSingle = Double.parseDouble(val);
            val = props.getProperty("priceDouble");
            if (val != null) priceDouble = Double.parseDouble(val);
            val = props.getProperty("priceDeluxe");
            if (val != null) priceDeluxe = Double.parseDouble(val);
            val = props.getProperty("gstRate");
            if (val != null) gstRate = Double.parseDouble(val);

            System.out.println("[ConfigLoader] Configuration loaded from " + PROPS_FILE);
        } catch (IOException e) {
            System.err.println("[ConfigLoader] Failed to read " + PROPS_FILE + ": " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("[ConfigLoader] Invalid numeric value in config: " + e.getMessage());
        }
    }

    public String getDbUrl()       { return dbUrl; }
    public String getDbUser()      { return dbUser; }
    public String getDbPassword()  { return dbPassword; }
    public String getMailUsername() { return mailUsername; }
    public String getMailPassword(){ return mailPassword; }
    public double getPriceSingle() { return priceSingle; }
    public double getPriceDouble() { return priceDouble; }
    public double getPriceDeluxe() { return priceDeluxe; }
    public double getGstRate()     { return gstRate; }
}

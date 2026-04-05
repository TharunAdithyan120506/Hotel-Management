package com.mitgrandregency.hotel.service;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Handles Aadhaar card image file I/O.
 * <p>
 * Saves images to the {@code data/aadhaar/} directory on disk and returns
 * relative file paths for storage in the database.
 * </p>
 */
public class AadhaarStorageService {

    private static final String AADHAAR_DIR = "data/aadhaar";

    public AadhaarStorageService() {
        ensureDirectory();
    }

    private void ensureDirectory() {
        File dir = new File(AADHAAR_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Saves the given image bytes to a file and returns the relative path.
     *
     * @param imageData  raw JPEG/PNG bytes from the file chooser
     * @param roomNumber the room number (used in filename)
     * @param guestName  the guest name (used in filename)
     * @return relative path like "data/aadhaar/101_JohnDoe.jpg"
     * @throws IOException if the file cannot be written
     */
    public String saveAadhaarImage(byte[] imageData, String roomNumber, String guestName)
            throws IOException {
        ensureDirectory();
        String safeName = guestName.replaceAll("[^a-zA-Z0-9]", "");
        String fileName = roomNumber + "_" + safeName + ".jpg";
        Path filePath = Paths.get(AADHAAR_DIR, fileName);
        Files.write(filePath, imageData);
        return filePath.toString();
    }

    /**
     * Saves an Aadhaar image from a source file (copy) and returns the relative path.
     *
     * @param sourceFile the source image file selected by the user
     * @param roomNumber the room number
     * @param guestName  the guest name
     * @return relative path to the stored copy
     * @throws IOException if the file cannot be copied
     */
    public String saveAadhaarFromFile(File sourceFile, String roomNumber, String guestName)
            throws IOException {
        ensureDirectory();
        String safeName = guestName.replaceAll("[^a-zA-Z0-9]", "");
        String extension = getExtension(sourceFile.getName());
        String fileName = roomNumber + "_" + safeName + extension;
        Path targetPath = Paths.get(AADHAAR_DIR, fileName);
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toString();
    }

    /**
     * Loads an Aadhaar image from the given path for display in the UI.
     *
     * @param relativePath the path stored in the database
     * @return a JavaFX {@link Image}, or {@code null} if the file doesn't exist
     */
    public Image loadAadhaarImage(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return null;
        File file = new File(relativePath);
        if (!file.exists()) return null;
        try (FileInputStream fis = new FileInputStream(file)) {
            return new Image(fis);
        } catch (IOException e) {
            System.err.println("Could not load Aadhaar image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks whether an Aadhaar image file exists at the given path.
     */
    public boolean exists(String relativePath) {
        return relativePath != null && !relativePath.isEmpty() && new File(relativePath).exists();
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}

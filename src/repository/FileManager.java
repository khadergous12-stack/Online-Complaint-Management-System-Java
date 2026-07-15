package repository;

import exception.ComplaintSystemException;
import model.Complaint;
import model.User;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file-based persistence for the application.
 * Users and complaints are stored as plain pipe-delimited text
 * files under the data/ directory so the whole project runs
 * without any external database.
 */
public class FileManager {

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.txt";
    private static final String COMPLAINTS_FILE = DATA_DIR + File.separator + "complaints.txt";
    private static final String SEQUENCE_FILE = DATA_DIR + File.separator + "sequence.txt";
    private static final String LOG_FILE = "logs" + File.separator + "app.log";

    public FileManager() {
        ensureDataFiles();
    }

    private void ensureDataFiles() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get("logs"));
            Files.createDirectories(Paths.get("outputs"));
            if (!Files.exists(Paths.get(USERS_FILE))) {
                Files.createFile(Paths.get(USERS_FILE));
            }
            if (!Files.exists(Paths.get(COMPLAINTS_FILE))) {
                Files.createFile(Paths.get(COMPLAINTS_FILE));
            }
            if (!Files.exists(Paths.get(SEQUENCE_FILE))) {
                Files.write(Paths.get(SEQUENCE_FILE), "0".getBytes());
            }
        } catch (IOException e) {
            System.out.println("Warning: could not initialize data files - " + e.getMessage());
        }
    }

    // ---------- USERS ----------

    public List<User> loadUsers() throws ComplaintSystemException {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    users.add(User.fromFileString(line));
                }
            }
        } catch (IOException e) {
            throw new ComplaintSystemException("Failed to load users file: " + e.getMessage(), e);
        }
        return users;
    }

    public void saveUser(User user) throws ComplaintSystemException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            bw.write(user.toFileString());
            bw.newLine();
        } catch (IOException e) {
            throw new ComplaintSystemException("Failed to save user: " + e.getMessage(), e);
        }
        log("USER_CREATED: " + user.getEmail());
    }

    // ---------- COMPLAINTS ----------

    public List<Complaint> loadComplaints() throws ComplaintSystemException {
        List<Complaint> complaints = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(COMPLAINTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    complaints.add(Complaint.fromFileString(line));
                }
            }
        } catch (IOException e) {
            throw new ComplaintSystemException("Failed to load complaints file: " + e.getMessage(), e);
        }
        return complaints;
    }

    /**
     * Rewrites the entire complaints file from the given list.
     * Used after any update (status change, assignment, resolution,
     * feedback) since we are working with plain text, not a database.
     */
    public void saveAllComplaints(List<Complaint> complaints) throws ComplaintSystemException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(COMPLAINTS_FILE, false))) {
            for (Complaint c : complaints) {
                bw.write(c.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ComplaintSystemException("Failed to save complaints: " + e.getMessage(), e);
        }
    }

    // ---------- SEQUENCE (complaint ID counter) ----------

    public int loadSequence() throws ComplaintSystemException {
        try {
            String content = new String(Files.readAllBytes(Paths.get(SEQUENCE_FILE))).trim();
            return content.isEmpty() ? 0 : Integer.parseInt(content);
        } catch (IOException e) {
            throw new ComplaintSystemException("Failed to load complaint ID sequence: " + e.getMessage(), e);
        }
    }

    public void saveSequence(int value) throws ComplaintSystemException {
        try {
            Files.write(Paths.get(SEQUENCE_FILE), String.valueOf(value).getBytes());
        } catch (IOException e) {
            throw new ComplaintSystemException("Failed to save complaint ID sequence: " + e.getMessage(), e);
        }
    }

    // ---------- LOGGING ----------

    public void log(String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            bw.write(java.time.LocalDateTime.now() + " - " + message);
            bw.newLine();
        } catch (IOException ignored) {
            // Logging failures should never crash the app.
        }
    }
}

package service;

import exception.ComplaintSystemException;
import model.User;
import repository.FileManager;
import utility.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles registration, login and lookup of users.
 * Keeps an in-memory cache backed by FileManager for persistence.
 */
public class UserService {

    private final FileManager fileManager;
    private final List<User> users;
    private int nextUserId;

    public UserService(FileManager fileManager) throws ComplaintSystemException {
        this.fileManager = fileManager;
        this.users = new ArrayList<>(fileManager.loadUsers());
        this.nextUserId = users.stream().mapToInt(User::getUserId).max().orElse(1000) + 1;
        seedAdminIfMissing();
    }

    /** Ensures there is always at least one admin account to log in with. */
    private void seedAdminIfMissing() throws ComplaintSystemException {
        boolean hasAdmin = users.stream().anyMatch(u -> u.getRole() == User.Role.ADMIN);
        if (!hasAdmin) {
            User admin = new User(nextUserId++, "Admin", "admin@complaints.local", "admin123", User.Role.ADMIN);
            users.add(admin);
            fileManager.saveUser(admin);
        }
    }

    public User register(String name, String email, String password) throws ComplaintSystemException {
        if (!AppUtils.isNotBlank(name)) {
            throw new ComplaintSystemException("Name cannot be empty.");
        }
        if (!AppUtils.isValidEmail(email)) {
            throw new ComplaintSystemException("Email format is invalid.");
        }
        if (!AppUtils.isValidPassword(password)) {
            throw new ComplaintSystemException("Password must be at least 4 characters.");
        }
        if (findByEmail(email).isPresent()) {
            throw new ComplaintSystemException("This email is already registered.");
        }
        User user = new User(nextUserId++, name.trim(), email.trim(), password, User.Role.USER);
        users.add(user);
        fileManager.saveUser(user);
        return user;
    }

    public User login(String email, String password) throws ComplaintSystemException {
        User user = findByEmail(email)
                .orElseThrow(() -> new ComplaintSystemException("Invalid email or password."));
        if (!user.getPassword().equals(password)) {
            throw new ComplaintSystemException("Invalid email or password.");
        }
        fileManager.log("LOGIN: " + user.getEmail() + " (" + user.getRole() + ")");
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    public Optional<User> findById(int userId) {
        return users.stream().filter(u -> u.getUserId() == userId).findFirst();
    }
}

package model;

/**
 * Represents a system user, either a normal complaint-raising USER
 * or an ADMIN who triages and resolves complaints.
 *
 * Demonstrates encapsulation: all fields are private and only
 * accessible through getters/setters.
 */
public class User {

    /** Role of the user inside the system. */
    public enum Role {
        USER,
        ADMIN
    }

    private int userId;
    private String name;
    private String email;
    private String password;
    private Role role;

    public User(int userId, String name, String email, String password, Role role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Serializes this user into a single pipe-delimited line for
     * plain-text file persistence (see repository.FileManager).
     * Format: userId|name|email|password|role
     */
    public String toFileString() {
        return userId + "|" + name + "|" + email + "|" + password + "|" + role;
    }

    /**
     * Reconstructs a User object from a line produced by toFileString().
     */
    public static User fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        int id = Integer.parseInt(parts[0].trim());
        String name = parts[1];
        String email = parts[2];
        String password = parts[3];
        Role role = Role.valueOf(parts[4].trim());
        return new User(id, name, email, password, role);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}

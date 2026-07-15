package main;

import exception.ComplaintSystemException;
import model.*;
import repository.FileManager;
import service.ComplaintService;
import service.UserService;

import java.util.List;
import java.util.Scanner;

/**
 * Entry point of the Online Complaint Management System.
 * Provides a console menu that routes to a User dashboard or an
 * Admin dashboard depending on who logs in.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static UserService userService;
    private static ComplaintService complaintService;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" ONLINE COMPLAINT MANAGEMENT SYSTEM");
        System.out.println("==================================================");

        try {
            FileManager fileManager = new FileManager();
            userService = new UserService(fileManager);
            complaintService = new ComplaintService(fileManager);
        } catch (ComplaintSystemException e) {
            System.out.println("Fatal error starting application: " + e.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\n1. Register\n2. Login\n3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    handleRegister();
                    break;
                case "2":
                    handleLogin();
                    break;
                case "3":
                    running = false;
                    System.out.println("Thank you for using the Complaint Management System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        sc.close();
    }

    // ---------- AUTH ----------

    private static void handleRegister() {
        try {
            System.out.print("Full name: ");
            String name = sc.nextLine();
            System.out.print("Email: ");
            String email = sc.nextLine();
            System.out.print("Password (min 4 chars): ");
            String password = sc.nextLine();
            User user = userService.register(name, email, password);
            System.out.println("Registration successful. Your User ID is " + user.getUserId() + ". Please login.");
        } catch (ComplaintSystemException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static void handleLogin() {
        try {
            System.out.print("Email: ");
            String email = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();
            User user = userService.login(email, password);
            System.out.println("Login successful. Welcome, " + user.getName() + " (" + user.getRole() + ")");
            if (user.getRole() == User.Role.ADMIN) {
                adminDashboard(user);
            } else {
                userDashboard(user);
            }
        } catch (ComplaintSystemException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    // ---------- USER DASHBOARD ----------

    private static void userDashboard(User user) {
        boolean back = false;
        while (!back) {
            System.out.println("\n---- USER DASHBOARD (" + user.getName() + ") ----");
            System.out.println("1. Submit a new complaint");
            System.out.println("2. View my complaints");
            System.out.println("3. Track complaint status by ID");
            System.out.println("4. Give feedback on a resolved complaint");
            System.out.println("5. Close a resolved complaint");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        submitComplaintFlow(user);
                        break;
                    case "2":
                        viewMyComplaints(user);
                        break;
                    case "3":
                        trackComplaint();
                        break;
                    case "4":
                        giveFeedbackFlow(user);
                        break;
                    case "5":
                        closeComplaintFlow(user);
                        break;
                    case "6":
                        back = true;
                        System.out.println("Logged out.");
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (ComplaintSystemException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
    }

    private static void submitComplaintFlow(User user) throws ComplaintSystemException {
        System.out.print("Title: ");
        String title = sc.nextLine();
        System.out.print("Description: ");
        String description = sc.nextLine();
        ComplaintCategory category = chooseEnum("Category", ComplaintCategory.values());
        ComplaintPriority priority = chooseEnum("Priority", ComplaintPriority.values());
        Complaint c = complaintService.submitComplaint(user.getUserId(), title, description, category, priority);
        System.out.println("Complaint submitted successfully! Your Complaint ID is: " + c.getComplaintId());
    }

    private static void viewMyComplaints(User user) {
        List<Complaint> list = complaintService.getComplaintsForUser(user.getUserId());
        if (list.isEmpty()) {
            System.out.println("You have not submitted any complaints yet.");
        } else {
            list.forEach(System.out::println);
        }
    }

    private static void trackComplaint() throws ComplaintSystemException {
        System.out.print("Enter Complaint ID: ");
        String id = sc.nextLine();
        Complaint c = complaintService.findById(id);
        System.out.println(c);
    }

    private static void giveFeedbackFlow(User user) throws ComplaintSystemException {
        System.out.print("Enter Complaint ID: ");
        String id = sc.nextLine();
        System.out.print("Your feedback: ");
        String feedback = sc.nextLine();
        complaintService.addFeedback(id, user.getUserId(), feedback);
        System.out.println("Feedback recorded. Thank you!");
    }

    private static void closeComplaintFlow(User user) throws ComplaintSystemException {
        System.out.print("Enter Complaint ID: ");
        String id = sc.nextLine();
        complaintService.closeComplaint(id, user.getUserId());
        System.out.println("Complaint closed successfully.");
    }

    // ---------- ADMIN DASHBOARD ----------

    private static void adminDashboard(User admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n---- ADMIN DASHBOARD (" + admin.getName() + ") ----");
            System.out.println("1. View all complaints");
            System.out.println("2. Filter complaints (status/category/priority)");
            System.out.println("3. Assign a complaint to myself");
            System.out.println("4. Update complaint status");
            System.out.println("5. Add resolution note (marks Resolved)");
            System.out.println("6. Search complaint by ID");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        complaintService.getAllComplaints().forEach(System.out::println);
                        break;
                    case "2":
                        filterFlow();
                        break;
                    case "3":
                        assignFlow(admin);
                        break;
                    case "4":
                        updateStatusFlow();
                        break;
                    case "5":
                        resolveFlow();
                        break;
                    case "6":
                        trackComplaint();
                        break;
                    case "7":
                        back = true;
                        System.out.println("Logged out.");
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (ComplaintSystemException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
    }

    private static void filterFlow() {
        ComplaintStatus status = chooseOptionalEnum("Status", ComplaintStatus.values());
        ComplaintCategory category = chooseOptionalEnum("Category", ComplaintCategory.values());
        ComplaintPriority priority = chooseOptionalEnum("Priority", ComplaintPriority.values());
        List<Complaint> result = complaintService.filter(status, category, priority);
        if (result.isEmpty()) {
            System.out.println("No complaints match that filter.");
        } else {
            result.forEach(System.out::println);
        }
    }

    private static void assignFlow(User admin) throws ComplaintSystemException {
        System.out.print("Enter Complaint ID: ");
        String id = sc.nextLine();
        complaintService.assignComplaint(id, admin.getName());
        System.out.println("Complaint assigned to " + admin.getName() + ".");
    }

    private static void updateStatusFlow() throws ComplaintSystemException {
        System.out.print("Enter Complaint ID: ");
        String id = sc.nextLine();
        ComplaintStatus status = chooseEnum("New status", ComplaintStatus.values());
        complaintService.updateStatus(id, status);
        System.out.println("Status updated to " + status + ".");
    }

    private static void resolveFlow() throws ComplaintSystemException {
        System.out.print("Enter Complaint ID: ");
        String id = sc.nextLine();
        System.out.print("Resolution note: ");
        String note = sc.nextLine();
        complaintService.addResolution(id, note);
        System.out.println("Complaint marked as Resolved.");
    }

    // ---------- SMALL CONSOLE HELPERS ----------

    private static <T extends Enum<T>> T chooseEnum(String label, T[] values) {
        while (true) {
            System.out.println(label + " options:");
            for (int i = 0; i < values.length; i++) {
                System.out.println("  " + (i + 1) + ". " + values[i]);
            }
            System.out.print("Choose " + label + " (number): ");
            String input = sc.nextLine().trim();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < values.length) {
                    return values[index];
                }
            } catch (NumberFormatException ignored) {
                // fall through to error message below
            }
            System.out.println("Invalid choice, please try again.");
        }
    }

    /** Same as chooseEnum but allows skipping (returns null) by pressing Enter. */
    private static <T extends Enum<T>> T chooseOptionalEnum(String label, T[] values) {
        System.out.println(label + " options (press Enter to skip):");
        for (int i = 0; i < values.length; i++) {
            System.out.println("  " + (i + 1) + ". " + values[i]);
        }
        System.out.print("Choose " + label + " (number or Enter to skip): ");
        String input = sc.nextLine().trim();
        if (input.isEmpty()) return null;
        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < values.length) {
                return values[index];
            }
        } catch (NumberFormatException ignored) {
        }
        System.out.println("Invalid choice, skipping this filter.");
        return null;
    }
}

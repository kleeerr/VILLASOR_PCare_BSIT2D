
package com.pcare.ui;

import com.pcare.dao.UserDao;
import com.pcare.dao.ActivityLogDao;
import com.pcare.model.User;
import com.pcare.model.ActivityLog;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this(userDao, scanner, null);
    }

    public AdminMenu(UserDao userDao, Scanner scanner,
                     User currentAdmin) {

        this.userDao = userDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
        this.activityLogDao = new ActivityLogDao();
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n==================================");
            System.out.println("          ADMIN MENU");
            System.out.println("==================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("0. Back to Previous Menu");
            System.out.println("==================================");

            int choice = readIntInput("Enter your choice: ");

            switch (choice) {
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUser();
                case 4 -> updateUserRoleAndStatus();
                case 5 -> showAuditLogMenu();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    public void viewAllUsers() {
        List<User> users = userDao.getAllUsers();

        System.out.println("\n========== ALL SYSTEM USERS ==========");
        displayUsers(users);
    }

    public void viewPendingUsers() {
        List<User> users = userDao.getPendingUsers();

        System.out.println("\n========== PENDING REGISTRATIONS ==========");
        displayUsers(users);
    }

    public void approveUser() {
        List<User> pendingUsers = userDao.getPendingUsers();

        System.out.println("\n========== APPROVE USER ACCOUNT ==========");
        displayUsers(pendingUsers);

        if (pendingUsers.isEmpty()) {
            System.out.println("There are no pending accounts to approve.");
            return;
        }

        System.out.println("Enter 0 to cancel.");
        int userId = readIntInput("Enter the User ID to approve: ");

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        User selectedUser = findUserById(pendingUsers, userId);

        if (selectedUser == null) {
            System.out.println(
                    "User ID not found in pending registrations."
            );
            return;
        }

        boolean approved = userDao.approveUser(userId);

        if (approved) {
            System.out.println(
                    "SUCCESS! Account for username '"
                            + selectedUser.getUsername()
                            + "' (ID: " + userId
                            + ") has been approved."
            );

            System.out.println("New status: APPROVED");

            recordAdminActivity(
                    "USER_APPROVED",
                    "Approved account for user ID " + userId
                            + " (" + selectedUser.getUsername() + ")"
            );

        } else {
            System.out.println("Approval failed. Please try again.");
        }
    }

    public void updateUserRoleAndStatus() {
        List<User> users = userDao.getAllUsers();

        System.out.println("\n========== UPDATE USER ROLE AND STATUS ==========");
        displayUsers(users);

        if (users.isEmpty()) {
            System.out.println("No users are available to update.");
            return;
        }

        int targetUserId = readIntInput(
                "Enter the User ID to update (0 to cancel): "
        );

        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        User targetUser = findUserById(users, targetUserId);

        if (targetUser == null) {
            System.out.println("User ID not found. No changes made.");
            return;
        }

        if (currentAdmin != null
                && targetUserId == currentAdmin.getUserId()
                && "ACTIVE".equalsIgnoreCase(currentAdmin.getStatus())) {

            System.out.println(
                    "WARNING: Modifying the currently active admin account is restricted."
            );
            return;
        }

        System.out.println(
                "Selected user: " + targetUser.getUsername()
        );

        String oldRole = targetUser.getRole();
        String oldStatus = targetUser.getStatus();

        String newRole = readValidRole();

        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();

        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        boolean updated = userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus
        );

        if (updated) {
            System.out.println("\nSUCCESS! User updated successfully.");
            System.out.println("User ID: " + targetUserId);
            System.out.println("New Role: " + newRole);
            System.out.println("New Status: " + newStatus);

            recordAdminActivity(
                    "USER_UPDATED",
                    "Updated user ID " + targetUserId
                            + ": Role " + oldRole + " to " + newRole
                            + ", Status " + oldStatus + " to " + newStatus
            );

        } else {
            System.out.println(
                    "Update failed. Please check the database."
            );
        }
    }

    // AUDIT LOG MENU
    private void showAuditLogMenu() {

        while (true) {
            System.out.println("\n==================================");
            System.out.println("       SYSTEM AUDIT LOGS");
            System.out.println("==================================");
            System.out.println("1. View All Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("0. Back");
            System.out.println("==================================");

            int choice = readIntInput("Enter your choice: ");

            switch (choice) {
                case 1 -> viewAllAuditLogs();

                case 2 -> {
                    int userId = readIntInput(
                            "Enter User ID (0 to cancel): "
                    );

                    if (userId != 0) {
                        viewAuditLogsByUser(userId);
                    }
                }

                case 3 -> clearHistoricalLogs();

                case 0 -> {
                    return;
                }

                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // VIEW ALL AUDIT LOGS
    private void viewAllAuditLogs() {
        List<ActivityLog> logs = activityLogDao.getAllLogs();

        System.out.println("\n========== ALL SYSTEM AUDIT LOGS ==========");
        displayAuditLogs(logs);
    }

    // FILTER LOGS BY USER ID
    private void viewAuditLogsByUser(int userId) {
        List<ActivityLog> logs =
                activityLogDao.getLogsByUserId(userId);

        System.out.println(
                "\n========== AUDIT LOGS FOR USER ID "
                        + userId + " =========="
        );

        displayAuditLogs(logs);
    }

    // CLEAR LOGS WITH CONFIRMATION
    private void clearHistoricalLogs() {

        System.out.println("\n========== CLEAR HISTORICAL LOGS ==========");
        System.out.println(
                "WARNING: This will permanently delete all existing audit logs."
        );

        System.out.print("Are you sure? Enter YES to confirm: ");
        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("YES")) {
            System.out.println("Log clearing cancelled.");
            return;
        }

        boolean cleared = activityLogDao.clearAllLogs();

        if (cleared) {
            System.out.println("Historical audit logs cleared successfully.");

            recordAdminActivity(
                    "AUDIT_LOGS_CLEARED",
                    "Administrator cleared historical audit logs."
            );

        } else {
            System.out.println("Unable to clear historical audit logs.");
        }
    }

    // DISPLAY AUDIT LOGS
    private void displayAuditLogs(List<ActivityLog> logs) {

        System.out.printf(
                "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                "ID", "User ID", "Action Type",
                "Description", "Timestamp"
        );

        System.out.println(
                "-----------------------------------------------------------------------------------------------"
        );

        if (logs.isEmpty()) {
            System.out.println("No audit logs found.");
            return;
        }

        for (ActivityLog log : logs) {
            System.out.printf(
                    "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                    log.getLogId(),
                    log.getUserId(),
                    shorten(log.getActionType(), 18),
                    shorten(log.getDescription(), 35),
                    log.getTimestamp()
            );
        }

        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("Total logs: " + logs.size());
    }

    // RECORD ADMIN ACTIVITY
    private void recordAdminActivity(
            String actionType, String description) {

        if (currentAdmin != null) {
            activityLogDao.logAction(
                    currentAdmin.getUserId(),
                    actionType,
                    description
            );
        }
    }

    public int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input! Please enter numbers only."
                );
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println("\nAvailable roles:");
            System.out.println("1. Administrator");
            System.out.println("2. Technician");
            System.out.println("3. Laboratory Assistant");
            System.out.println("0. Cancel");

            int choice = readIntInput("Choose the new role: ");

            switch (choice) {
                case 1:
                    return "ADMINISTRATOR";
                case 2:
                    return "TECHNICIAN";
                case 3:
                    return "LABORATORY ASSISTANT";
                case 0:
                    return null;
                default:
                    System.out.println("Invalid role choice. Please try again.");
            }
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.println("\nAvailable statuses:");
            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");
            System.out.println("0. Cancel");

            int choice = readIntInput("Choose the new status: ");

            switch (choice) {
                case 1:
                    return "APPROVED";
                case 2:
                    return "PENDING";
                case 0:
                    return null;
                default:
                    System.out.println("Invalid status choice. Please try again.");
            }
        }
    }

    private User findUserById(List<User> users, int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }

        return null;
    }

    private void displayUsers(List<User> users) {
        String border =
                "+-------+-----------------+------------------------+--------------+";

        System.out.println(border);
        System.out.printf(
                "| %-5s | %-15s | %-22s | %-12s |%n",
                "ID", "Username", "Role", "Status"
        );
        System.out.println(border);

        if (users.isEmpty()) {
            System.out.printf(
                    "| %-5s | %-15s | %-22s | %-12s |%n",
                    "-", "No users found", "-", "-"
            );
        } else {
            for (User user : users) {
                System.out.printf(
                        "| %-5s | %-15s | %-22s | %-12s |%n",
                        user.getUserId(),
                        shorten(user.getUsername(), 15),
                        shorten(user.getRole(), 22),
                        shorten(user.getStatus(), 12)
                );
            }
        }

        System.out.println(border);
        System.out.println("Total accounts: " + users.size());
    }

    private String shorten(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }
}

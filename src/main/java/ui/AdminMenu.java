package com.pcare.ui;

import com.pcare.dao.UserDao;
import com.pcare.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this(userDao, scanner, null);
    }

    public AdminMenu(
            UserDao userDao,
            Scanner scanner,
            User currentAdmin) {

        this.userDao = userDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
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
            System.out.println("0. Back to Previous Menu");
            System.out.println("==================================");

            int choice = readIntInput("Enter your choice (0 to cancel): ");

            switch (choice) {
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUser();
                case 4 -> updateUserRoleAndStatus();
                case 0 -> {
                    return;
                }
                default -> System.out.println(
                        "Invalid choice. Please choose 0 to 4."
                );
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
            System.out.println(
                    "There are no pending accounts to approve."
            );
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
        } else {
            System.out.println(
                    "Approval failed. Please try again."
            );
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
                && "ACTIVE".equalsIgnoreCase(
                currentAdmin.getStatus())) {

            System.out.println(
                    "WARNING: Modifying the currently active admin account is restricted."
            );
            return;
        }

        System.out.println(
                "Selected user: " + targetUser.getUsername()
        );

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
        } else {
            System.out.println(
                    "Update failed. Please check the database."
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

            int choice = readIntInput(
                    "Choose the new role: "
            );

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
                    System.out.println(
                            "Invalid role choice. Please try again."
                    );
            }
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.println("\nAvailable statuses:");
            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");
            System.out.println("0. Cancel");

            int choice = readIntInput(
                    "Choose the new status: "
            );

            switch (choice) {
                case 1:
                    return "APPROVED";
                case 2:
                    return "PENDING";
                case 0:
                    return null;
                default:
                    System.out.println(
                            "Invalid status choice. Please try again."
                    );
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
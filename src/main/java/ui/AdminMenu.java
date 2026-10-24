
package com.pcare.ui;

import com.pcare.dao.UserDao;
import com.pcare.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n==================================");
            System.out.println("          ADMIN MENU");
            System.out.println("==================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("0. Back to Previous Menu");
            System.out.println("==================================");

            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Display every registered account
    public void viewAllUsers() {
        List<User> users = userDao.getAllUsers();

        System.out.println("\n========== ALL SYSTEM USERS ==========");
        displayUsers(users);
    }

    // Display only accounts awaiting approval
    public void viewPendingUsers() {
        List<User> users = userDao.getPendingUsers();

        System.out.println("\n========== PENDING REGISTRATIONS ==========");
        displayUsers(users);
    }

    // Approve a pending account
    public void approveUser() {
        List<User> pendingUsers = userDao.getPendingUsers();

        System.out.println("\n========== APPROVE USER ACCOUNT ==========");
        displayUsers(pendingUsers);

        if (pendingUsers.isEmpty()) {
            System.out.println("There are no pending accounts to approve.");
            return;
        }

        System.out.println("Enter 0 to cancel.");
        System.out.print("Enter the User ID to approve: ");

        String input = scanner.nextLine().trim();

        if (input.equals("0")) {
            System.out.println("Approval cancelled.");
            return;
        }

        int userId;

        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid User ID.");
            return;
        }

        User selectedUser = null;

        for (User user : pendingUsers) {
            if (user.getUserId() == userId) {
                selectedUser = user;
                break;
            }
        }

        if (selectedUser == null) {
            System.out.println(
                    "User ID not found in the pending registrations. No changes made."
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
                    "Approval failed. The account may already have been updated."
            );
        }
    }

    // Formatted table for all user lists
    private void displayUsers(List<User> users) {
        String border =
                "+-------+-----------------+--------------+------------+";

        System.out.println(border);
        System.out.printf(
                "| %-5s | %-15s | %-12s | %-10s |%n",
                "ID", "Username", "Role", "Status"
        );
        System.out.println(border);

        if (users.isEmpty()) {
            System.out.printf(
                    "| %-5s | %-15s | %-12s | %-10s |%n",
                    "-", "No users found", "-", "-"
            );
        } else {
            for (User user : users) {
                System.out.printf(
                        "| %-5s | %-15s | %-12s | %-10s |%n",
                        user.getUserId(),
                        shorten(user.getUsername(), 15),
                        shorten(user.getRole(), 12),
                        shorten(user.getStatus(), 10)
                );
            }
        }

        System.out.println(border);
        System.out.println("Total accounts: " + users.size());
    }

    // Prevent long values from breaking the table layout
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

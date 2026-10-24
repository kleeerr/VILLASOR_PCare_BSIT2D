
package com.pcare.ui;

import com.pcare.dao.UserDao;
import com.pcare.model.User;
import com.pcare.service.AuthenticationService;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final AuthenticationService authService;
    private final Scanner scanner;

    public Authentication(
            UserDao userDao,
            AuthenticationService authService,
            Scanner scanner) {

        this.userDao = userDao;
        this.authService = authService;
        this.scanner = scanner;
    }

    public User login() {

        System.out.println("\n========== PCare LOGIN ==========");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = authService.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return null;
        }

        if (!user.getStatus().equalsIgnoreCase("Active")) {
            System.out.println("Your account status is "
                    + user.getStatus()
                    + ". Please contact the administrator.");
            return null;
        }

        System.out.println("\nLogin successful! Welcome, "
                + user.getFullName() + "!");
        System.out.println("Role: " + user.getRole());

        return user;
    }

    public void register() {

        System.out.println("\n========== REGISTER ACCOUNT ==========");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine().trim();

        if (username.isBlank() || password.isBlank()
                || fullName.isBlank()) {
            System.out.println("All fields are required.");
            return;
        }

        System.out.println("\nSelect your role:");
        System.out.println("1. Technician");
        System.out.println("2. Laboratory Assistant");

        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        String role;

        switch (choice) {
            case "1" -> role = "Technician";
            case "2" -> role = "Laboratory Assistant";
            default -> {
                System.out.println("Invalid role selection.");
                return;
            }
        }

        User newUser = new User(
                username,
                password,
                fullName,
                role
        );

        if (userDao.registerUser(newUser)) {
            System.out.println("\nRegistration successful!");
            System.out.println("Account status: PENDING");
            System.out.println(
                    "Please wait for the administrator to activate your account."
            );
        } else {
            System.out.println(
                    "Registration failed. The username may already exist."
            );
        }
    }
}

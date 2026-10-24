
package com.pcare.service;

import com.pcare.dao.UserDao;
import com.pcare.model.User;

public class AuthenticationService {

    private final UserDao userDao;

    public AuthenticationService() {
        userDao = new UserDao();
    }

    public User login(String username, String password) {

        if (username == null || password == null
                || username.isBlank() || password.isBlank()) {
            System.out.println("Username and password are required.");
            return null;
        }

        for (User user : userDao.getAllUsers()) {

            if (user.getUsername().equals(username)
                    && user.getPassword().equals(password)) {

                String status = user.getStatus();

                if ("PENDING".equalsIgnoreCase(status)) {
                    System.out.println(
                            "Your account is waiting for administrator approval."
                    );
                    return null;
                }

                if ("INACTIVE".equalsIgnoreCase(status)) {
                    System.out.println("Your account is inactive.");
                    return null;
                }

                if (!"ACTIVE".equalsIgnoreCase(status)
                        && !"APPROVED".equalsIgnoreCase(status)) {
                    System.out.println(
                            "Your account is not approved or active."
                    );
                    return null;
                }

                if (!isValidRole(user.getRole())) {
                    System.out.println("Invalid account role.");
                    return null;
                }

                return user;
            }
        }

        System.out.println("Incorrect username or password.");
        return null;
    }

    private boolean isValidRole(String role) {
        return role != null
                && (role.equals("Administrator")
                || role.equals("Technician")
                || role.equals("Laboratory Assistant"));
    }
}

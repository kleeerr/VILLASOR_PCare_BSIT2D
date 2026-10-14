package com.pcare;

import com.pcare.dao.UserDao;
import com.pcare.model.User;

public class SetupAdmin {

    public static void main(String[] args) {

        UserDao userDao = new UserDao();

        User admin = new User(
                "admin",
                "Admin123!",
                "System Administrator",
                "Administrator"
        );

        boolean success = userDao.addUser(admin);

        if (success) {
            System.out.println("Administrator account created successfully!");
            System.out.println("Username: admin");
            System.out.println("Password: Admin123!");
        } else {
            System.out.println("Failed to create administrator account.");
        }
    }
}
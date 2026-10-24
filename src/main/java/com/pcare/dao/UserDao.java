
package com.pcare.dao;

import com.pcare.database.DatabaseConnection;
import com.pcare.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // CREATE: Add a new user (used by Administrator)
    public boolean addUser(User user) {
        String sql = """
                INSERT INTO Users (username, password, full_name, role, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getRole());
            statement.setString(5, user.getStatus());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    // REGISTER: New accounts require administrator approval
    public boolean registerUser(User user) {
        String sql = """
                INSERT INTO Users (username, password, full_name, role, status)
                VALUES (?, ?, ?, ?, 'PENDING')
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getRole());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    // READ: Get all users, regardless of role or status
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM Users ORDER BY user_id";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(mapUser(result));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving users: " + e.getMessage());
        }

        return users;
    }

    // READ: Get all users whose status is PENDING
    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT * FROM Users
                WHERE UPPER(status) = 'PENDING'
                ORDER BY user_id
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(mapUser(result));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving pending users: " + e.getMessage());
        }

        return users;
    }

    // APPROVE: Change a pending user's status to APPROVED
    public boolean approveUser(int userId) {
        String sql = """
                UPDATE Users
                SET status = 'APPROVED'
                WHERE user_id = ?
                AND UPPER(status) = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    // READ: Find a user by ID
    public User getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapUser(result);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error finding user: " + e.getMessage());
        }

        return null;
    }

    // UPDATE: Edit user information
    public boolean updateUser(User user) {
        String sql = """
                UPDATE Users
                SET username = ?, password = ?, full_name = ?, role = ?, status = ?
                WHERE user_id = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getRole());
            statement.setString(5, user.getStatus());
            statement.setInt(6, user.getUserId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    // DELETE: Remove a user by ID
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    // UPDATE: Activate or deactivate a user
    public boolean updateUserStatus(int userId, String status) {
        if (!status.equals("Active") && !status.equals("Inactive")) {
            System.out.println("Invalid status. Use Active or Inactive.");
            return false;
        }

        String sql = "UPDATE Users SET status = ? WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    // HELPER: Convert a database row into a User object
    private User mapUser(ResultSet result) throws SQLException {
        return new User(
                result.getInt("user_id"),
                result.getString("username"),
                result.getString("password"),
                result.getString("full_name"),
                result.getString("role"),
                result.getString("status")
        );
    }
}


package com.pcare.dao;

import com.pcare.database.DatabaseConnection;
import com.pcare.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // CREATE: Record an activity
    public boolean logAction(int userId, String actionType,
                             String description) {

        String sql = """
                INSERT INTO Activity_Logs
                    (user_id, action_type, description, timestamp)
                VALUES (?, ?, ?, ?)
                """;

        String timestamp = LocalDateTime.now().format(FORMATTER);

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, actionType);
            statement.setString(3, description);
            statement.setString(4, timestamp);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Error recording activity: " + e.getMessage()
            );
            return false;
        }
    }

    // Compatibility method: Categorize existing Main.java log messages
    public boolean addLog(int userId, String action) {

        String actionType;

        if (action.equalsIgnoreCase("Logged in")) {
            actionType = "USER_LOGIN";

        } else if (action.equalsIgnoreCase("Logged out")) {
            actionType = "USER_LOGOUT";

        } else if (action.startsWith("Added user:")) {
            actionType = "USER_ADDED";

        } else if (action.startsWith("Updated user ID:")) {
            actionType = "USER_UPDATED";

        } else if (action.startsWith("Active user ID:")
                || action.startsWith("Inactive user ID:")) {
            actionType = "USER_STATUS_UPDATED";

        } else if (action.startsWith("Deleted user ID:")) {
            actionType = "USER_DELETED";

        } else if (action.startsWith("Added computer:")) {
            actionType = "COMPUTER_ADDED";

        } else if (action.startsWith("Updated condition of computer ID:")) {
            actionType = "COMPUTER_CONDITION_UPDATED";

        } else if (action.startsWith("Updated computer ID:")) {
            actionType = "COMPUTER_UPDATED";

        } else if (action.startsWith("Deleted computer ID:")) {
            actionType = "COMPUTER_DELETED";

        } else if (action.startsWith("Assigned technician ")) {
            actionType = "TECHNICIAN_ASSIGNED";

        } else if (action.startsWith("Changed request ")
                || action.startsWith("Updated assigned request ")) {
            actionType = "REQUEST_STATUS_UPDATED";

        } else if (action.startsWith("Updated repair notes for request ")
                || action.startsWith("Recorded repair notes for request ")) {
            actionType = "REPAIR_NOTES_UPDATED";

        } else if (action.startsWith("Updated problem description for request ")) {
            actionType = "PROBLEM_UPDATED";

        } else if (action.startsWith("Deleted maintenance request ")) {
            actionType = "REQUEST_DELETED";

        } else if (action.startsWith("Generated summary report")) {
            actionType = "REPORT_GENERATED";

        } else if (action.startsWith("Reported a problem for computer ID:")) {
            actionType = "REQUEST_CREATED";

        } else {
            actionType = "GENERAL_ACTIVITY";
        }

        return logAction(userId, actionType, action);
    }

    // READ: View all logs
    public List<ActivityLog> getAllLogs() {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM Activity_Logs
                ORDER BY timestamp DESC, log_id DESC
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                logs.add(mapLog(result));
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error retrieving activity logs: " + e.getMessage()
            );
        }

        return logs;
    }

    // READ: View logs by user ID
    public List<ActivityLog> getLogsByUserId(int userId) {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM Activity_Logs
                WHERE user_id = ?
                ORDER BY timestamp DESC, log_id DESC
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    logs.add(mapLog(result));
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error retrieving user activity: " + e.getMessage()
            );
        }

        return logs;
    }

    // Compatibility method
    public List<ActivityLog> getLogsByUser(int userId) {
        return getLogsByUserId(userId);
    }

    // READ: View logs within a date range
    public List<ActivityLog> getLogsByDateRange(
            String startDate, String endDate) {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM Activity_Logs
                WHERE timestamp BETWEEN ? AND ?
                ORDER BY timestamp DESC, log_id DESC
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, startDate + " 00:00:00");
            statement.setString(2, endDate + " 23:59:59");

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    logs.add(mapLog(result));
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error retrieving logs by date: " + e.getMessage()
            );
        }

        return logs;
    }

    // DELETE: Clear historical logs
    public boolean clearAllLogs() {

        String sql = "DELETE FROM Activity_Logs";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println(
                    "Error clearing activity logs: " + e.getMessage()
            );
            return false;
        }
    }

    // Convert database record into ActivityLog object
    private ActivityLog mapLog(ResultSet result)
            throws SQLException {

        return new ActivityLog(
                result.getInt("log_id"),
                result.getInt("user_id"),
                result.getString("action_type"),
                result.getString("description"),
                result.getString("timestamp")
        );
    }
}

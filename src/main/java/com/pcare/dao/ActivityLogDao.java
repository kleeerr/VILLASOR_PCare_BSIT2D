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

    // CREATE: Record a user activity
    public boolean addLog(int userId, String action) {

        String sql = """
                INSERT INTO Activity_Logs (user_id, action, date_time)
                VALUES (?, ?, ?)
                """;

        String dateTime = LocalDateTime.now().format(FORMATTER);

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, action);
            statement.setString(3, dateTime);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error recording activity: " + e.getMessage());
            return false;
        }
    }

    // READ: View all activity logs
    public List<ActivityLog> getAllLogs() {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM Activity_Logs
                ORDER BY log_id DESC
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                logs.add(new ActivityLog(
                        result.getInt("log_id"),
                        result.getInt("user_id"),
                        result.getString("action"),
                        result.getString("date_time")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving activity logs: "
                    + e.getMessage());
        }

        return logs;
    }

    // READ: View activity logs for one user
    public List<ActivityLog> getLogsByUser(int userId) {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM Activity_Logs
                WHERE user_id = ?
                ORDER BY log_id DESC
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    logs.add(new ActivityLog(
                            result.getInt("log_id"),
                            result.getInt("user_id"),
                            result.getString("action"),
                            result.getString("date_time")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving user activity: "
                    + e.getMessage());
        }

        return logs;
    }

    // READ: View logs within a date range
    public List<ActivityLog> getLogsByDateRange(
            String startDate, String endDate) {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM Activity_Logs
                WHERE date_time BETWEEN ? AND ?
                ORDER BY log_id DESC
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, startDate + " 00:00:00");
            statement.setString(2, endDate + " 23:59:59");

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    logs.add(new ActivityLog(
                            result.getInt("log_id"),
                            result.getInt("user_id"),
                            result.getString("action"),
                            result.getString("date_time")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving logs by date: "
                    + e.getMessage());
        }

        return logs;
    }
}
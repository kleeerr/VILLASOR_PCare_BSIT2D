
package com.pcare.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:pcare.db";

    public static Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }

        return connection;
    }

    public static void initializeDatabase() {

        String usersTable = """
                CREATE TABLE IF NOT EXISTS Users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'Active'
                );
                """;

        String computersTable = """
                CREATE TABLE IF NOT EXISTS Computers (
                    computer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    computer_name TEXT NOT NULL,
                    location TEXT NOT NULL,
                    specifications TEXT,
                    condition TEXT NOT NULL DEFAULT 'Good',
                    date_registered TEXT NOT NULL
                );
                """;

        String maintenanceTable = """
                CREATE TABLE IF NOT EXISTS Maintenance_Requests (
                    request_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    computer_id INTEGER NOT NULL,
                    reported_by INTEGER NOT NULL,
                    assigned_to INTEGER,
                    problem_description TEXT NOT NULL,
                    date_reported TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'Pending',
                    repair_notes TEXT,
                    date_completed TEXT,
                    FOREIGN KEY (computer_id)
                        REFERENCES Computers(computer_id),
                    FOREIGN KEY (reported_by)
                        REFERENCES Users(user_id),
                    FOREIGN KEY (assigned_to)
                        REFERENCES Users(user_id)
                );
                """;

        String activityLogsTable = """
                CREATE TABLE IF NOT EXISTS Activity_Logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    action TEXT NOT NULL,
                    date_time TEXT NOT NULL,
                    FOREIGN KEY (user_id)
                        REFERENCES Users(user_id)
                );
                """;

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.execute(usersTable);
            statement.execute(computersTable);
            statement.execute(maintenanceTable);
            statement.execute(activityLogsTable);

            System.out.println(
                    "PCare database initialized successfully!"
            );

        } catch (SQLException e) {
            System.out.println(
                    "Database initialization error: " + e.getMessage()
            );
            throw new IllegalStateException(
                    "Could not initialize the PCare database.", e
            );
        }
    }
}
